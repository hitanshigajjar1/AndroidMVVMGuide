# Android MVVM Architecture Guide 📱

A comprehensive guide and reference implementation demonstrating the **Model-View-ViewModel (MVVM)** architecture pattern in Android development.

---

## 📑 Table of Contents

1. [What is MVVM?](#what-is-mvvm)
2. [Architecture Overview](#architecture-overview)
3. [Project Structure](#project-structure)
4. [Layer-by-Layer Explanation](#layer-by-layer-explanation)
5. [Data Flow](#data-flow)
6. [Implementation Guide](#implementation-guide)
7. [Code Examples](#code-examples)
8. [Best Practices](#best-practices)
9. [Testing Strategy](#testing-strategy)
10. [Common Patterns](#common-patterns)

---

## 🎯 What is MVVM?

**MVVM (Model-View-ViewModel)** is an architectural pattern that separates your application into three main components:

```
┌─────────────────────────────────────────────────────┐
│                      VIEW                           │
│            (Activity/Fragment/XML)                  │
│                                                     │
│  • Displays data                                    │
│  • Captures user input                             │
│  • No business logic                               │
└──────────────────┬──────────────────────────────────┘
                   │ Observes
                   │ (LiveData/Flow)
                   ↓
┌─────────────────────────────────────────────────────┐
│                   VIEWMODEL                         │
│                                                     │
│  • Holds UI state                                   │
│  • Business logic                                   │
│  • Survives configuration changes                  │
│  • No Android framework dependencies               │
└──────────────────┬──────────────────────────────────┘
                   │ Requests data
                   ↓
┌─────────────────────────────────────────────────────┐
│                     MODEL                           │
│              (Repository + Data Sources)            │
│                                                     │
│  • Data operations                                  │
│  • Business rules                                   │
│  • Network/Database access                         │
└─────────────────────────────────────────────────────┘
```

### Why MVVM?

✅ **Separation of Concerns** - Each layer has a single responsibility  
✅ **Testability** - Easy to unit test ViewModels and Repository  
✅ **Maintainability** - Changes in one layer don't affect others  
✅ **Lifecycle Awareness** - ViewModel survives configuration changes  
✅ **Reactive UI** - Automatic UI updates with LiveData/Flow  

---

## 🏗️ Architecture Overview

### The Three Layers

#### 1. **View Layer** (UI)
- **Components**: Activities, Fragments, XML layouts
- **Responsibility**: Display data and capture user input
- **Rule**: Contains NO business logic

#### 2. **ViewModel Layer** (Presentation Logic)
- **Components**: ViewModel classes
- **Responsibility**: Prepare data for UI, handle user actions
- **Rule**: No reference to View (Activity/Fragment)

#### 3. **Model Layer** (Data)
- **Components**: Repository, Data Sources (API, Database)
- **Responsibility**: Provide data to ViewModel
- **Rule**: Single source of truth for data

### Additional Components

#### Repository Pattern
- Mediates between different data sources
- Implements caching strategy
- Provides clean API to ViewModel

#### Data Sources
- **Remote**: API calls (Retrofit)
- **Local**: Database (Room)
- **Preferences**: SharedPreferences/DataStore

---

## 📁 Project Structure

```
app/src/main/java/com/ext/androidmvvmguide/
│
├── 📂 data/                          # MODEL LAYER
│   ├── 📂 model/                     # Domain models (business objects)
│   │   └── User.kt                   # POJO/Data class used in UI
│   │
│   ├── 📂 remote/                    # Remote data source
│   │   ├── ApiService.kt             # Retrofit interface
│   │   └── UserResponse.kt           # API response models
│   │
│   ├── 📂 local/                     # Local data source
│   │   ├── AppDatabase.kt            # Room database
│   │   ├── UserDao.kt                # Database queries
│   │   └── UserEntity.kt             # Database table model
│   │
│   └── 📂 repository/                # Repository pattern
│       └── UserRepository.kt         # Mediates data sources
│
├── 📂 ui/                            # VIEW & VIEWMODEL LAYER
│   ├── 📂 main/                      # Feature: User List
│   │   ├── MainFragment.kt           # VIEW - Displays list
│   │   ├── MainViewModel.kt          # VIEWMODEL - List logic
│   │   ├── MainUiState.kt            # UI state definition
│   │   └── UserAdapter.kt            # RecyclerView adapter
│   │
│   ├── 📂 details/                   # Feature: User Details
│   │   ├── DetailsFragment.kt        # VIEW - Displays details
│   │   ├── DetailsViewModel.kt       # VIEWMODEL - Details logic
│   │   └── DetailsUiState.kt         # UI state definition
│   │
│   └── 📂 common/                    # Shared UI components
│       ├── BaseFragment.kt           # Base fragment with common code
│       ├── BaseViewModel.kt          # Base ViewModel
│       └── UiState.kt                # Generic UI state
│
├── 📂 utils/                         # UTILITIES
│   ├── Result.kt                     # Wrapper for success/error
│   ├── Constants.kt                  # App constants
│   └── Extensions.kt                 # Kotlin extensions
│
├── App.kt                            # Application class
└── MainActivity.kt                   # Host activity
```

---

## 🔍 Layer-by-Layer Explanation

### 1️⃣ DATA LAYER (Model)

The data layer is responsible for all data operations.

#### 📦 **data/model/** - Domain Models

**Purpose**: Business objects used throughout the app

```kotlin
data class User(
    val id: Int,
    val name: String,
    val email: String
)
```

**Why separate from Entity/Response?**
- Clean separation between data sources and business logic
- UI layer doesn't depend on database or API structure
- Easy to change data sources without affecting UI

---

#### 🌐 **data/remote/** - Remote Data Source

**Purpose**: Handle network operations

```kotlin
// API Interface
interface ApiService {
    @GET("users")
    suspend fun getUsers(): List<UserResponse>
}

// API Response Model
data class UserResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String
) {
    fun toDomainModel() = User(id, name, email)
}
```

**Key Points:**
- Use Retrofit for HTTP requests
- Response models map JSON to Kotlin objects
- Convert to domain models before returning
- Use `suspend` functions for coroutines

---

#### 💾 **data/local/** - Local Data Source

**Purpose**: Handle database operations

```kotlin
// Room Database
@Database(entities = [UserEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}

// DAO - Data Access Object
@Dao
interface UserDao {
    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<UserEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>)
}

// Entity - Database Table
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: Int,
    val name: String
) {
    fun toDomainModel() = User(id, name, email)
}
```

**Key Points:**
- Room handles SQLite operations
- Entities represent database tables
- DAOs define database queries
- Use Flow for reactive updates
- Convert to domain models before returning

---

#### 🏪 **data/repository/** - Repository Pattern

**Purpose**: Single source of truth, coordinates data sources

```kotlin
class UserRepository(
    private val apiService: ApiService,
    private val userDao: UserDao
) {
    fun getUsers(forceRefresh: Boolean = false): Flow<Result<List<User>>> = flow {
        emit(Result.Loading)
        
        // Strategy 1: Try cache first
        if (!forceRefresh) {
            val cached = userDao.getAllUsers().first()
            if (cached.isNotEmpty()) {
                emit(Result.Success(cached.toDomainModel()))
                return@flow
            }
        }
        
        // Strategy 2: Fetch from API
        try {
            val response = apiService.getUsers()
            val users = response.toDomainModel()
            
            // Update cache
            userDao.insertUsers(users.toEntity())
            
            emit(Result.Success(users))
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }
}
```

**Repository Responsibilities:**
1. ✅ Decide where to fetch data (cache vs network)
2. ✅ Implement caching strategy
3. ✅ Handle errors
4. ✅ Convert data to domain models
5. ✅ Provide clean API to ViewModel

**Common Caching Strategies:**
- **Cache First**: Return cache, then fetch fresh data
- **Network First**: Fetch from network, fallback to cache on error
- **Cache Only**: Only use local data
- **Network Only**: Always fetch fresh data

---

### 2️⃣ VIEWMODEL LAYER (Presentation Logic)

The ViewModel prepares data for the UI and handles user actions.

#### 🎨 **UI State Pattern**

```kotlin
sealed class MainUiState {
    object Idle : MainUiState()
    object Loading : MainUiState()
    data class Success(val users: List<User>) : MainUiState()
    data class Error(val message: String) : MainUiState()
}
```

**Why Sealed Classes?**
- Type-safe state representation
- Exhaustive when() checks
- Easy to add new states
- Clear intent

---

#### 🧠 **ViewModel Implementation**

```kotlin
class MainViewModel(
    private val repository: UserRepository
) : ViewModel() {
    
    // Private mutable state
    private val _uiState = MutableLiveData<MainUiState>(MainUiState.Idle)
    
    // Public immutable state
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
```

**ViewModel Characteristics:**
- ✅ Survives configuration changes (rotation)
- ✅ Lifecycle-aware (cleaned up automatically)
- ✅ No reference to View
- ✅ Exposes immutable state to UI
- ✅ Uses viewModelScope for coroutines

**Common Anti-Patterns to AVOID:**
- ❌ Passing Context to ViewModel
- ❌ Holding reference to View/Activity/Fragment
- ❌ Accessing UI elements directly
- ❌ Making ViewModel lifecycle-dependent

---

### 3️⃣ VIEW LAYER (UI)

The View displays data and captures user input.

#### 🖼️ **Fragment Implementation**

```kotlin
class MainFragment : Fragment() {
    
    // ViewBinding for type-safe view access
    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!
    
    // ViewModel instance
    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory(App.instance.userRepository)
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
                is MainUiState.Idle -> showIdle()
                is MainUiState.Loading -> showLoading()
                is MainUiState.Success -> showSuccess(state.users)
                is MainUiState.Error -> showError(state.message)
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Prevent memory leaks
    }
}
```

**View Responsibilities:**
1. ✅ Inflate layout
2. ✅ Setup UI components
3. ✅ Observe ViewModel state
4. ✅ Update UI based on state
5. ✅ Delegate user actions to ViewModel

**View MUST NOT:**
- ❌ Contain business logic
- ❌ Make network calls
- ❌ Access database directly
- ❌ Format data (ViewModel's job)

---

## 🔄 Data Flow

### Complete Flow Example: Loading Users

```
┌─────────────────────────────────────────────────────────────┐
│ 1. USER ACTION                                              │
│    User clicks "Load Users" button                          │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ↓
┌─────────────────────────────────────────────────────────────┐
│ 2. VIEW (Fragment)                                          │
│    binding.btnLoad.setOnClickListener {                     │
│        viewModel.loadUsers()  ← Calls ViewModel method      │
│    }                                                         │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ↓
┌─────────────────────────────────────────────────────────────┐
│ 3. VIEWMODEL                                                │
│    fun loadUsers() {                                        │
│        _uiState.value = MainUiState.Loading                 │
│        viewModelScope.launch {                              │
│            repository.getUsers().collect { result ->        │
│                _uiState.value = mapToUiState(result)        │
│            }                                                 │
│        }                                                     │
│    }                                                         │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ↓
┌─────────────────────────────────────────────────────────────┐
│ 4. REPOSITORY                                               │
│    fun getUsers(): Flow<Result<List<User>>> = flow {        │
│        emit(Result.Loading)                                 │
│                                                             │
│        // Check cache first                                 │
│        val cached = userDao.getAllUsers().first()           │
│        if (cached.isNotEmpty()) {                           │
│            emit(Result.Success(cached))                     │
│            return@flow                                      │
│        }                                                     │
│                                                             │
│        // Fetch from API                                    │
│        val response = apiService.getUsers()                 │
│        userDao.insertUsers(response)                        │
│        emit(Result.Success(response))                       │
│    }                                                         │
└────────────────────┬────────────────────────────────────────┘
                     │
        ┌────────────┴────────────┐
        ↓                         ↓
┌──────────────────┐    ┌──────────────────┐
│ 5a. LOCAL DB     │    │ 5b. REMOTE API   │
│ Room Database    │    │ Retrofit         │
│ userDao.query()  │    │ apiService.get() │
└──────────────────┘    └──────────────────┘
        │                         │
        └────────────┬────────────┘
                     ↓
┌─────────────────────────────────────────────────────────────┐
│ 6. DATA FLOWS BACK                                          │
│    Result → Repository → ViewModel → UiState                │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ↓
┌─────────────────────────────────────────────────────────────┐
│ 7. VIEW OBSERVES CHANGE                                     │
│    viewModel.uiState.observe { state ->                     │
│        when (state) {                                       │
│            is MainUiState.Loading -> showLoading()          │
│            is MainUiState.Success -> showUsers(state.users) │
│            is MainUiState.Error -> showError(state.message) │
│        }                                                     │
│    }                                                         │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ↓
┌─────────────────────────────────────────────────────────────┐
│ 8. UI UPDATES                                               │
│    RecyclerView displays users                              │
│    Progress bar hides                                       │
└─────────────────────────────────────────────────────────────┘
```

---

## 🛠️ Implementation Guide

### Step 1: Setup Dependencies

```kotlin
// build.gradle.kts (app)
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

---

### Step 2: Create Domain Model

```kotlin
// data/model/User.kt
data class User(
    val id: Int,
    val name: String,
    val email: String,
    val phone: String
)
```

---

### Step 3: Setup Remote Data Source

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
    @SerializedName("email") val email: String,
    @SerializedName("phone") val phone: String
)

// Extension function for conversion
fun UserResponse.toDomainModel() = User(id, name, email, phone)
```

---

### Step 4: Setup Local Data Source

```kotlin
// data/local/UserEntity.kt
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val email: String,
    val phone: String
)

fun UserEntity.toDomainModel() = User(id, name, email, phone)

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

---

### Step 5: Create Repository

```kotlin
// data/repository/UserRepository.kt
class UserRepository(
    private val apiService: ApiService,
    private val userDao: UserDao
) {
    fun getUsers(forceRefresh: Boolean = false): Flow<Result<List<User>>> = flow {
        emit(Result.Loading)
        
        if (!forceRefresh) {
            val cached = userDao.getAllUsers().first()
            if (cached.isNotEmpty()) {
                emit(Result.Success(cached.map { it.toDomainModel() }))
                return@flow
            }
        }
        
        try {
            val response = apiService.getUsers()
            val users = response.map { it.toDomainModel() }
            userDao.insertUsers(users.map { it.toEntity() })
            emit(Result.Success(users))
        } catch (e: Exception) {
            emit(Result.Error(e, e.message ?: "Error loading users"))
        }
    }
}
```

---

### Step 6: Create UI State

```kotlin
// ui/main/MainUiState.kt
sealed class MainUiState {
    object Idle : MainUiState()
    object Loading : MainUiState()
    data class Success(val users: List<User>) : MainUiState()
    data class Error(val message: String) : MainUiState()
}
```

---

### Step 7: Create ViewModel

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

// ViewModelFactory for dependency injection
class MainViewModelFactory(
    private val repository: UserRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MainViewModel(repository) as T
    }
}
```

---

### Step 8: Create Fragment

```kotlin
// ui/main/MainFragment.kt
class MainFragment : Fragment() {
    
    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory(App.instance.userRepository)
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
        
        // Setup UI
        binding.btnLoad.setOnClickListener {
            viewModel.loadUsers()
        }
        
        // Observe ViewModel
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
                    // Update RecyclerView with state.users
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

## 💡 Best Practices

### 1. Naming Conventions

```kotlin
// Models
User.kt              // Domain model
UserEntity.kt        // Database model
UserResponse.kt      // API model

// ViewModels
MainViewModel.kt     // Feature name + ViewModel

// Fragments
MainFragment.kt      // Feature name + Fragment

// UI States
MainUiState.kt       // Feature name + UiState
```

### 2. Package Organization

```
✅ GOOD: Organize by feature
ui/
├── main/           # All files for main feature
├── details/        # All files for details feature
└── profile/        # All files for profile feature

❌ BAD: Organize by type
ui/
├── fragments/      # All fragments mixed together
├── viewmodels/     # All viewmodels mixed together
└── adapters/       # All adapters mixed together
```

### 3. State Management

```kotlin
// ✅ GOOD: Single state object
sealed class UiState {
    object Loading : UiState()
    data class Success(val data: Data) : UiState()
    data class Error(val message: String) : UiState()
}

// ❌ BAD: Multiple LiveData objects
val isLoading = MutableLiveData<Boolean>()
val error = MutableLiveData<String>()
val data = MutableLiveData<Data>()
```

### 4. Error Handling

```kotlin
// ✅ GOOD: Centralized error handling in Repository
try {
    val response = apiService.getUsers()
    Result.Success(response)
} catch (e: Exception) {
    Result.Error(e, "Failed to load users")
}

// ❌ BAD: Letting exceptions bubble up
val response = apiService.getUsers() // Crashes if network error
```

### 5. Memory Leaks Prevention

```kotlin
// ✅ GOOD: Null binding in onDestroyView
override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
}

// ✅ GOOD: Use viewLifecycleOwner for observations
viewModel.uiState.observe(viewLifecycleOwner) { ... }

// ❌ BAD: Use 'this' as lifecycle owner
viewModel.uiState.observe(this) { ... }
```

---

## 🧪 Testing Strategy

### Unit Testing ViewModel

```kotlin
class MainViewModelTest {
    
    private lateinit var viewModel: MainViewModel
    private lateinit var repository: UserRepository
    
    @Before
    fun setup() {
        repository = mockk()
        viewModel = MainViewModel(repository)
    }
    
    @Test
    fun `loadUsers should emit Loading then Success`() = runTest {
        // Given
        val users = listOf(User(1, "John", "john@example.com", "123"))
        coEvery { repository.getUsers() } returns flowOf(
            Result.Loading,
            Result.Success(users)
        )
        
        // When
        viewModel.loadUsers()
        
        // Then
        assertEquals(MainUiState.Loading, viewModel.uiState.value)
        // Wait for coroutine
        advanceUntilIdle()
        assertEquals(MainUiState.Success(users), viewModel.uiState.value)
    }
}
```

### Unit Testing Repository

```kotlin
class UserRepositoryTest {
    
    private lateinit var repository: UserRepository
    private lateinit var apiService: ApiService
    private lateinit var userDao: UserDao
    
    @Test
    fun `getUsers should return cached data first`() = runTest {
        // Given
        val cachedUsers = listOf(UserEntity(1, "John", "john@example.com", "123"))
        coEvery { userDao.getAllUsers() } returns flowOf(cachedUsers)
        
        // When
        val result = repository.getUsers(forceRefresh = false).first()
        
        // Then
        assertTrue(result is Result.Success)
        assertEquals(1, (result as Result.Success).data.size)
        coVerify(exactly = 0) { apiService.getUsers() } // API not called
    }
}
```

---

## 🎨 Common Patterns

### 1. Single Event Pattern

For one-time events like navigation or showing toast:

```kotlin
class SingleEvent<out T>(private val content: T) {
    
    private var hasBeenHandled = false
    
    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }
}

// ViewModel
private val _navigateToDetails = MutableLiveData<SingleEvent<Int>>()
val navigateToDetails: LiveData<SingleEvent<Int>> = _navigateToDetails

fun onUserClick(userId: Int) {
    _navigateToDetails.value = SingleEvent(userId)
}

// Fragment
viewModel.navigateToDetails.observe(viewLifecycleOwner) { event ->
    event.getContentIfNotHandled()?.let { userId ->
        // Navigate only once
        findNavController().navigate(toDetails(userId))
    }
}
```

### 2. Pagination Pattern

```kotlin
class MainViewModel(private val repository: UserRepository) : ViewModel() {
    
    private var currentPage = 1
    private val allUsers = mutableListOf<User>()
    
    fun loadNextPage() {
        viewModelScope.launch {
            repository.getUsers(page = currentPage)
                .collect { result ->
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

### 3. Search Pattern

```kotlin
class MainViewModel(private val repository: UserRepository) : ViewModel() {
    
    val searchQuery = MutableLiveData<String>()
    
    val searchResults: LiveData<List<User>> = searchQuery.switchMap { query ->
        liveData {
            repository.searchUsers(query)
                .collect { result ->
                    if (result is Result.Success) {
                        emit(result.data)
                    }
                }
        }
    }
}
```

---

## 📚 Key Takeaways

### ✅ DO's

1. **Keep ViewModels Android-free** - No Context, no View references
2. **Use sealed classes for state** - Type-safe and exhaustive
3. **Convert models at boundaries** - Response → Entity → Domain
4. **Handle errors in Repository** - Don't let them crash the app
5. **Observe with viewLifecycleOwner** - Prevent memory leaks
6. **Null ViewBinding in onDestroyView** - Clean up properly
7. **Use Flow for reactive streams** - Modern and efficient
8. **Test ViewModels and Repository** - Easy to unit test

### ❌ DON'Ts

1. **Don't pass View to ViewModel** - Breaks separation of concerns
2. **Don't use ViewModel in Repository** - Wrong direction of dependency
3. **Don't put business logic in Fragment** - Belongs in ViewModel
4. **Don't make direct API/DB calls from Fragment** - Use Repository
5. **Don't hold Activity context in ViewModel** - Causes memory leaks
6. **Don't use GlobalScope** - Use viewModelScope instead
7. **Don't expose MutableLiveData** - Expose LiveData only
8. **Don't create ViewModel manually** - Use by viewModels() delegate

---

## 🚀 Quick Start Checklist

### Starting a New Feature

- [ ] 1. Create domain model in `data/model/`
- [ ] 2. Create API response model in `data/remote/`
- [ ] 3. Add API endpoint in `ApiService`
- [ ] 4. Create database entity in `data/local/`
- [ ] 5. Add database queries in `DAO`
- [ ] 6. Implement repository method
- [ ] 7. Create UI state sealed class
- [ ] 8. Create ViewModel with factory
- [ ] 9. Create Fragment with ViewBinding
- [ ] 10. Setup observers and UI logic
- [ ] 11. Add navigation if needed
- [ ] 12. Write unit tests

---

## 🔧 Troubleshooting

### Common Issues

#### Issue: ViewModel not surviving rotation
**Solution**: Make sure you're using `by viewModels()` delegate, not creating manually

#### Issue: LiveData not updating UI
**Solution**: Check you're observing with `viewLifecycleOwner`, not `this`

#### Issue: Memory leaks
**Solution**: Null ViewBinding in `onDestroyView()` and use proper lifecycle owner

#### Issue: Coroutine not cancelling
**Solution**: Use `viewModelScope` instead of `GlobalScope` or custom scope

#### Issue: Database not updating
**Solution**: Make sure Room is emitting Flow or using suspend functions properly

---

## 📖 Additional Resources

### Official Documentation
- [Android Architecture Guide](https://developer.android.com/topic/architecture)
- [ViewModel Overview](https://developer.android.com/topic/libraries/architecture/viewmodel)
- [LiveData Overview](https://developer.android.com/topic/libraries/architecture/livedata)
- [Room Database](https://developer.android.com/training/data-storage/room)
- [Kotlin Coroutines](https://developer.android.com/kotlin/coroutines)
- [Kotlin Flow](https://developer.android.com/kotlin/flow)

### Sample Apps
- [Android Architecture Samples](https://github.com/android/architecture-samples)
- [Now in Android](https://github.com/android/nowinandroid)

---

## 🎓 Learning Path

### Beginner
1. ✅ Understand the three layers (View, ViewModel, Model)
2. ✅ Learn LiveData basics
3. ✅ Create simple ViewModel with one operation
4. ✅ Implement basic Repository pattern
5. ✅ Handle loading and error states

### Intermediate
1. ✅ Master coroutines and Flow
2. ✅ Implement complex UI states
3. ✅ Add local database caching
4. ✅ Handle pagination
5. ✅ Implement search functionality
6. ✅ Add dependency injection (Hilt/Koin)

### Advanced
1. ✅ Write comprehensive unit tests
2. ✅ Implement offline-first architecture
3. ✅ Add multi-module architecture
4. ✅ Optimize performance
5. ✅ Implement complex navigation flows
6. ✅ Migrate to Jetpack Compose with ViewModel

---

## 💻 Demo App Features

This project demonstrates:

✅ **User List Screen**
- Fetch users from API
- Display in RecyclerView
- Cache in Room database
- Pull to refresh
- Loading states
- Error handling

✅ **User Details Screen**
- Show individual user details
- Navigate from list
- Fetch from cache/API
- Back navigation

✅ **Architecture Components**
- ViewModel with Factory
- LiveData for state management
- Repository pattern
- Room for local storage
- Retrofit for networking
- Coroutines for async operations
- ViewBinding for type-safe views
- Navigation Component

---

## 🔄 Migration Guide

### From MVP to MVVM

**MVP:**
```kotlin
interface MainView {
    fun showLoading()
    fun showUsers(users: List<User>)
    fun showError(message: String)
}

class MainPresenter(
    private val view: MainView,
    private val repository: UserRepository
) {
    fun loadUsers() {
        view.showLoading()
        repository.getUsers { users ->
            view.showUsers(users)
        }
    }
}
```

**MVVM:**
```kotlin
class MainViewModel(
    private val repository: UserRepository
) : ViewModel() {
    
    private val _uiState = MutableLiveData<MainUiState>()
    val uiState: LiveData<MainUiState> = _uiState
    
    fun loadUsers() {
        viewModelScope.launch {
            _uiState.value = MainUiState.Loading
            repository.getUsers()
                .collect { result ->
                    _uiState.value = when (result) {
                        is Result.Success -> MainUiState.Success(result.data)
                        is Result.Error -> MainUiState.Error(result.message)
                    }
                }
        }
    }
}
```

**Key Differences:**
- No View interface needed
- ViewModel doesn't hold View reference
- Lifecycle-aware automatically
- State is preserved on rotation

---

## 📝 Code Snippets

### Creating Result Wrapper

```kotlin
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Throwable, val message: String) : Result<Nothing>()
    object Loading : Result<Nothing>()
}
```

### Extension Functions

```kotlin
// View extensions
fun View.show() { visibility = View.VISIBLE }
fun View.hide() { visibility = View.GONE }

// Fragment extensions
fun Fragment.showToast(message: String) {
    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
}

// Model conversions
fun UserResponse.toDomainModel() = User(id, name, email, phone)
fun User.toEntity() = UserEntity(id, name, email, phone)
```

### Base Classes

```kotlin
abstract class BaseFragment<VB : ViewBinding> : Fragment() {
    
    private var _binding: VB? = null
    protected val binding get() = _binding!!
    
    abstract fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): VB
    abstract fun setupUI()
    abstract fun setupObservers()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = getViewBinding(inflater, container)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        setupObservers()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
```

---

## 🎯 Summary

### What is MVVM?
An architectural pattern that separates UI (View) from business logic (ViewModel) and data (Model).

### Why use MVVM?
- ✅ Testable code
- ✅ Separation of concerns
- ✅ Lifecycle awareness
- ✅ Easier to maintain
- ✅ Reactive UI updates

### Core Principles
1. **View** observes ViewModel, never calls it directly except for actions
2. **ViewModel** doesn't know about View, only exposes state
3. **Model** (Repository) is the single source of truth for data
4. Data flows in one direction: Model → ViewModel → View
5. User actions flow up: View → ViewModel → Model

### Technology Stack
- **UI**: Fragments, ViewBinding, RecyclerView
- **Architecture**: ViewModel, LiveData, Repository
- **Async**: Coroutines, Flow
- **Network**: Retrofit, OkHttp
- **Database**: Room
- **Navigation**: Navigation Component

---

## 📧 Conclusion

This guide provides a comprehensive understanding of MVVM architecture in Android. By following these patterns and practices, you'll create:

- 🎯 Well-structured apps
- 🧪 Testable code
- 🔄 Maintainable projects
- 📱 Production-ready applications

**Remember**: MVVM is not about strict rules, but about principles. Adapt it to your needs while keeping the core concepts intact.

---
