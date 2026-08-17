package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {

    @Query("SELECT * FROM customers ORDER BY updatedAt DESC")
    fun getAllCustomers(): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE primaryPhone = :phone LIMIT 1")
    suspend fun getCustomerByPrimaryPhone(phone: String): CustomerEntity?

    @Query("SELECT * FROM customers WHERE primaryPhone = :phone LIMIT 1")
    fun observeCustomerByPrimaryPhone(phone: String): Flow<CustomerEntity?>

    @Query("SELECT * FROM customers")
    suspend fun getAllCustomersList(): List<CustomerEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: CustomerEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomers(customers: List<CustomerEntity>)

    @Update
    suspend fun updateCustomer(customer: CustomerEntity)

    @Delete
    suspend fun deleteCustomer(customer: CustomerEntity)

    @Query("DELETE FROM customers WHERE primaryPhone = :phone")
    suspend fun deleteCustomerByPhone(phone: String)

    @Query("SELECT COUNT(*) FROM customers")
    suspend fun getCustomerCount(): Int
}
