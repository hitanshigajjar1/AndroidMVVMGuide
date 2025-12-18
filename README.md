# Android MVVM Architecture Guide

A concise guide for implementing the Model-View-ViewModel (MVVM) architecture pattern in Android.

---

## What is MVVM?

MVVM separates your application into three main components:

```
VIEW (Activity/Fragment)
    ↓ observes
VIEWMODEL (Business Logic)
    ↓ requests data
MODEL (Repository + Data Sources)
```

**Benefits:**
- ✅ Separation of Concerns
- ✅ Testable Code
- ✅ Lifecycle Awareness
- ✅ Maintainable Architecture

---

## Project Structure

```
app/src/main/java/com/example/app/
│
├── data/                           # MODEL LAYER
│   ├── model/                      # Domain models
│   │   └── User.kt
│   ├── remote/                     # API data source
│   │   ├── ApiService.kt
│   │   └── UserResponse.kt
│   ├── local/                      # Database data source
│   │   ├── AppDatabase.kt
│   │   ├── UserDao.kt
│   │   └── UserEntity.kt
│   └── repository/
│       └── UserRepository.kt
│
├── ui/                             # VIEW & VIEWMODEL
│   ├── main/
│   │   ├── MainFragment.kt         # VIEW
│   │   ├── MainViewModel.kt        # VIEWMODEL
│   │   └── MainUiState.kt
│   └── details/
│       ├── DetailsFragment.kt
│       └── DetailsViewModel.kt
│
└── utils/
    ├── Result.kt
    └── Extensions.kt
```

---

## Implementation

### 1. Setup Dependencies

```kotlin
dependencies {
    // ViewModel & LiveData
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    
    // Room Database
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")
    
    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
}
```

### 2. Domain Model

```kotlin
// data/model/User.kt
data class User(
    val id: Int,
    val name: String,
    val email: String
)
```

### 3. Remote Data Source

```kotlin
// data/remote/ApiService.kt
interface ApiService {
    @GET("users")
    suspend fun getUsers(): List<UserResponse>
}

// data/remote/UserResponse.kt
data class UserResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String
)

fun UserResponse.toDomainModel() = User(id, name, email)
```

### 4. Local Data Source

```kotlin
// data/local/UserEntity.kt
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val email: String
)

fun UserEntity.toDomainModel() = User(id, name, email)

// data/local/UserDao.kt
@Dao
interface UserDao {
    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<UserEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>)
}

// data/local/AppDatabase.kt
@Database(entities = [UserEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}
```

### 5. Repository Pattern

```kotlin
// data/repository/UserRepository.kt
class UserRepository(
    private val apiService: ApiService,
    private val userDao: UserDao
) {
    fun getUsers(forceRefresh: Boolean = false): Flow<Result<List<User>>> = flow {
        emit(Result.Loading)
        
        // Check cache first
        if (!forceRefresh) {
            val cached = userDao.getAllUsers().first()
            if (cached.isNotEmpty()) {
                emit(Result.Success(cached.map { it.toDomainModel() }))
                return@flow
            }
        }
        
        // Fetch from API
        try {
            val response = apiService.getUsers()
            val users = response.map { it.toDomainModel() }
            userDao.insertUsers(users.map { it.toEntity() })
            emit(Result.Success(users))
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "Unknown error"))
        }
    }
}
```

### 6. Result Wrapper

```kotlin
// utils/Result.kt
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String) : Result<Nothing>()
    object Loading : Result<Nothing>()
}
```

### 7. UI State

```kotlin
// ui/main/MainUiState.kt
sealed class MainUiState {
    object Idle : MainUiState()
    object Loading : MainUiState()
    data class Success(val users: List<User>) : MainUiState()
    data class Error(val message: String) : MainUiState()
}
```

### 8. ViewModel

```kotlin
// ui/main/MainViewModel.kt
class MainViewModel(
    private val repository: UserRepository
) : ViewModel() {
    
    private val _uiState = MutableLiveData<MainUiState>(MainUiState.Idle)
    val uiState: LiveData<MainUiState> = _uiState
    
    fun loadUsers() {
        viewModelScope.launch {
            repository.getUsers()
                .collect { result ->
                    _uiState.value = when (result) {
                        is Result.Loading -> MainUiState.Loading
                        is Result.Success -> MainUiState.Success(result.data)
                        is Result.Error -> MainUiState.Error(result.message)
                    }
                }
        }
    }
}

// ViewModelFactory
class MainViewModelFactory(
    private val repository: UserRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MainViewModel(repository) as T
    }
}
```

### 9. Fragment (View)

```kotlin
// ui/main/MainFragment.kt
class MainFragment : Fragment() {
    
    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory(repository)
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        observeViewModel()
    }
    
    private fun setupUI() {
        binding.btnLoad.setOnClickListener {
            viewModel.loadUsers()
        }
    }
    
    private fun observeViewModel() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is MainUiState.Idle -> {
                    binding.progressBar.visibility = View.GONE
                }
                is MainUiState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
                is MainUiState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    // Update UI with state.users
                }
                is MainUiState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
```

---

## Data Flow

```
User Action → View → ViewModel → Repository → Data Source (API/DB)
                ↓                    ↓
            Observes              Returns
                ↓                    ↓
           UI Updates  ← LiveData ← Result
```

**Flow Example:**
1. User clicks button → `viewModel.loadUsers()`
2. ViewModel updates state to `Loading`
3. Repository checks cache, then fetches from API
4. Repository returns `Result.Success(users)`
5. ViewModel maps to `MainUiState.Success(users)`
6. View observes change and updates UI

---

## Best Practices

### ✅ DO's

- Keep ViewModels Android-free (no Context)
- Use sealed classes for UI state
- Convert models at boundaries (Response → Entity → Domain)
- Handle errors in Repository
- Observe with `viewLifecycleOwner`
- Null ViewBinding in `onDestroyView()`
- Use `viewModelScope` for coroutines
- Expose immutable LiveData only

### ❌ DON'Ts

- Don't pass View reference to ViewModel
- Don't use ViewModel in Repository
- Don't put business logic in Fragment
- Don't make direct API/DB calls from View
- Don't hold Activity context in ViewModel
- Don't use `GlobalScope`
- Don't expose `MutableLiveData` publicly

---

## Common Patterns

### Single Event Pattern

```kotlin
class SingleEvent<out T>(private val content: T) {
    private var hasBeenHandled = false
    
    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) null else {
            hasBeenHandled = true
            content
        }
    }
}
```

### Pagination Pattern

```kotlin
class MainViewModel(private val repository: UserRepository) : ViewModel() {
    private var currentPage = 1
    private val allUsers = mutableListOf<User>()
    
    fun loadNextPage() {
        viewModelScope.launch {
            repository.getUsers(page = currentPage).collect { result ->
                if (result is Result.Success) {
                    allUsers.addAll(result.data)
                    currentPage++
                    _uiState.value = MainUiState.Success(allUsers)
                }
            }
        }
    }
}
```

---

## Testing

### ViewModel Test

```kotlin
class MainViewModelTest {
    
    @Test
    fun `loadUsers should emit Loading then Success`() = runTest {
        // Given
        val users = listOf(User(1, "John", "john@example.com"))
        coEvery { repository.getUsers() } returns flowOf(
            Result.Loading,
            Result.Success(users)
        )
        
        // When
        viewModel.loadUsers()
        
        // Then
        assertEquals(MainUiState.Loading, viewModel.uiState.value)
        advanceUntilIdle()
        assertEquals(MainUiState.Success(users), viewModel.uiState.value)
    }
}
```

### Repository Test

```kotlin
class UserRepositoryTest {
    
    @Test
    fun `getUsers should return cached data first`() = runTest {
        // Given
        val cached = listOf(UserEntity(1, "John", "john@example.com"))
        coEvery { userDao.getAllUsers() } returns flowOf(cached)
        
        // When
        val result = repository.getUsers().first()
        
        // Then
        assertTrue(result is Result.Success)
        coVerify(exactly = 0) { apiService.getUsers() }
    }
}
```

---

## Quick Start Checklist

- [ ] Create domain model
- [ ] Setup API service and response model
- [ ] Setup database entity and DAO
- [ ] Implement repository
- [ ] Create UI state sealed class
- [ ] Create ViewModel with factory
- [ ] Create Fragment with ViewBinding
- [ ] Setup observers
- [ ] Write unit tests

---

## Troubleshooting

| Issue | Solution |
|-------|----------|
| ViewModel not surviving rotation | Use `by viewModels()` delegate |
| LiveData not updating | Use `viewLifecycleOwner` for observations |
| Memory leaks | Null ViewBinding in `onDestroyView()` |
| Coroutine not cancelling | Use `viewModelScope` |

---

## Resources

- [Android Architecture Guide](https://developer.android.com/topic/architecture)
- [ViewModel Overview](https://developer.android.com/topic/libraries/architecture/viewmodel)
- [LiveData Overview](https://developer.android.com/topic/libraries/architecture/livedata)
- [Room Database](https://developer.android.com/training/data-storage/room)
- [Kotlin Coroutines](https://developer.android.com/kotlin/coroutines)

---

