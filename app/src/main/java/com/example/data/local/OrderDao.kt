package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {

    @Query("SELECT * FROM orders ORDER BY sequenceNumber ASC")
    fun getAllOrders(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE orderCode = :code LIMIT 1")
    suspend fun getOrderByCode(code: String): OrderEntity?

    @Query("SELECT * FROM orders WHERE customerPhone = :phone ORDER BY createdAt DESC")
    fun getOrdersByCustomerPhone(phone: String): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE customerPhone = :phone AND status = 'DELIVERED' ORDER BY deliveredAt DESC")
    fun getDeliveredOrdersForCustomer(phone: String): Flow<List<OrderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrders(orders: List<OrderEntity>)

    @Update
    suspend fun updateOrder(order: OrderEntity)

    @Delete
    suspend fun deleteOrder(order: OrderEntity)

    @Query("DELETE FROM orders WHERE orderCode = :code")
    suspend fun deleteOrderByCode(code: String)

    @Query("DELETE FROM orders")
    suspend fun clearAllOrders()

    @Query("SELECT COUNT(*) FROM orders")
    suspend fun getOrderCount(): Int
}
