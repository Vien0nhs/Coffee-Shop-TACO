package com.example.taco.FirebaseAPI

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.type.DateTime
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime
import java.util.Date

data class Product(
    val productId: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val oldPrice: Double? = null,
    val image: String? = null // Thay đổi từ ByteArray thành String (URL hoặc base64)
)

data class OrderProduct(
    val orderId: String = "",
    val productId: String = "",
    val quantity: Int = 0,
    val totalPrice: Double = 0.0,
    val tablenumber: String = "",
    val note: String = "",
    val orderStatus: Boolean = false,
    val startOrderTime: Date = Date(),
    val orderCompletedTime: Date = Date(),
    val orderConfirm: Boolean = false,
    val orderCount: Int = 0
)
data class AccessCode(
    val accessCode: String
)
data class Table(
    val tableId: String = "",
    val tablenumber: String = "",
    val isOccupied: Boolean = false,
    val tableQuantity: Int = 0
)


data class TableOrder(
    val tableId: String = "",
    val tableName: String = "",
    val oderId: String = "",
    val productId: String = "",
    val tableTotalPrice: Double = 0.0
)

data class Revenue(
    val revenueId: String = "",
    val revenueDate: Date = Date(),
    val tableId: String = "",
    val totalRevenue: Double = 0.0
)

class FirestoreHelper {

    private val db = FirebaseFirestore.getInstance()

    // Add Product
    fun addProduct(product: Product) {
        val productMap = hashMapOf(
            "name" to product.name,
            "price" to product.price,
            "oldPrice" to product.oldPrice,
            "image" to product.image
        )
        db.collection("Product").add(productMap)
    }

    // Get All Products
    suspend fun getAllProducts(): List<Product> {
        val snapshot = db.collection("Product").get().await()
        return snapshot.documents.map { document ->
            Product(
                productId = document.id,
                name = document.getString("name") ?: "",
                price = document.getDouble("price") ?: 0.0,
                oldPrice = document.getDouble("oldPrice"),
                image = document.getString("image") // Thay đổi từ getBlob thành getString
            )
        }
    }

    // Get Product by ID
    suspend fun getProductById(productId: String): Product? {
        val document = db.collection("Product").document(productId).get().await()
        return if (document.exists()) {
            Product(
                productId = document.id,
                name = document.getString("name") ?: "",
                price = document.getDouble("price") ?: 0.0,
                oldPrice = document.getDouble("oldPrice"),
                image = document.getString("image") // Thay đổi từ getBlob thành getString
            )
        } else null
    }
    fun updateProductById(productId: String, updatedProduct: Product) {
        // Tạo một map chứa các trường cần cập nhật
        val updatedProductMap = hashMapOf<String, Any?>(
            "name" to updatedProduct.name,
            "price" to updatedProduct.price,
            "oldPrice" to updatedProduct.oldPrice,
            "image" to updatedProduct.image
        )

        // Cập nhật dữ liệu cho sản phẩm với productId trong Firestore
        db.collection("Product").document(productId)
            .update(updatedProductMap)
            .addOnSuccessListener {
                // Thành công
                println("Product updated successfully!")
            }
            .addOnFailureListener { e ->
                // Thất bại
                println("Error updating product: $e")
            }
    }

    // Add OrderProduct
    suspend fun addOrderProduct(orderProduct: OrderProduct, onComplete: (DocumentReference) -> Unit) {
        val orderProductMap = hashMapOf(
            "productId" to orderProduct.productId,
            "quantity" to orderProduct.quantity,
            "totalPrice" to orderProduct.totalPrice,
            "tablenumber" to orderProduct.tablenumber,
            "note" to orderProduct.note
        )

        val documentReference = db.collection("OrderProduct").add(orderProductMap).await()
        onComplete(documentReference)
    }


    // Get Order Products by Order ID
    suspend fun getOrderProductsByOrderId(orderId: String): List<OrderProduct> {
        val snapshot = db.collection("OrderProduct")
            .whereEqualTo("orderId", orderId)
            .get()
            .await()
        return snapshot.documents.map { document ->
            OrderProduct(
                orderId = document.getString("orderId") ?: "",
                productId = document.getString("productId") ?: "",
                quantity = document.getLong("quantity")?.toInt() ?: 0,
                totalPrice = document.getDouble("totalPrice") ?: 0.0
            )
        }
    }

    // Delete Product by ID
    fun deleteProductById(productId: String) {
        db.collection("Product").document(productId).delete()
    }
    fun updateOrderProductById(orderProductId: String, updatedOrderProduct: OrderProduct) {
        // Tạo một map chứa các trường cần cập nhật
        val updatedOrderProductMap = hashMapOf<String, Any?>(
            "productId" to updatedOrderProduct.productId,
            "quantity" to updatedOrderProduct.quantity,
            "totalPrice" to updatedOrderProduct.totalPrice,
            "tablenumber" to updatedOrderProduct.tablenumber,
            "note" to updatedOrderProduct.note
        )

        // Cập nhật dữ liệu cho OrderProduct với orderProductId trong Firestore
        db.collection("OrderProduct").document(orderProductId)
            .update(updatedOrderProductMap)
            .addOnSuccessListener {
                // Thành công
                println("OrderProduct updated successfully!")
            }
            .addOnFailureListener { e ->
                // Thất bại
                println("Error updating OrderProduct: $e")
            }
    }
    fun deleteOrderProductById(orderProductId: String) {
        db.collection("OrderProduct").document(orderProductId).delete()
            .addOnSuccessListener {
                // Thành công
                println("OrderProduct deleted successfully!")
            }
            .addOnFailureListener { e ->
                // Thất bại
                println("Error deleting OrderProduct: $e")
            }
    }


    // Delete All Orders
    fun deleteAllOrders() {
        db.collection("OrderProduct").get().addOnSuccessListener { result ->
            for (document in result) {
                db.collection("OrderProduct").document(document.id).delete()
            }
        }
    }
}
fun base64ToBitmap(base64String: String): Bitmap? {
    return try {
        val decodedString = Base64.decode(base64String, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
    } catch (e: IllegalArgumentException) {
        null
    }
}