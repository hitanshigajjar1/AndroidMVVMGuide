package com.ext.androidmvvmguide.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ext.androidmvvmguide.data.model.User

/**
 * Room database entity for User
 * Represents a table in the local database
 */
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: Int,
    val name: String,
    val username: String,
    val email: String,
    val phone: String,
    val website: String
)

/**
 * Extension function to convert Entity to Domain Model
 */
fun UserEntity.toDomainModel(): User {
    return User(
        id = id,
        name = name,
        username = username,
        email = email,
        phone = phone,
        website = website
    )
}

/**
 * Extension function to convert list of entities
 */
fun List<UserEntity>.toDomainModel(): List<User> {
    return map { it.toDomainModel() }
}

/**
 * Extension function to convert Domain Model to Entity
 */
fun User.toEntity(): UserEntity {
    return UserEntity(
        id = id,
        name = name,
        username = username,
        email = email,
        phone = phone,
        website = website
    )
}

/**
 * Extension function to convert list of domain models
 */
fun List<User>.toEntity(): List<UserEntity> {
    return map { it.toEntity() }
}