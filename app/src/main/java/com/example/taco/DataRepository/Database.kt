package com.example.taco.DataRepository

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.core.database.getDoubleOrNull

class DatabaseTACO(context: Context) : SQLiteOpenHelper(context, DBNAME, null, DBVERSION) {
    companion object {
        private const val DBNAME = "TACO.db"
        private const val DBVERSION = 2
        private const val SQL_CREATE_PRODUCT_TABLE =
            "CREATE TABLE Product(" +
                    "IdProduct INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "Name TEXT NOT NULL," +
                    "Price REAL NOT NULL," +
                    "OldPrice REAL," +
                    "Image BLOB" +
                    ")"
        private const val SQL_DROP_PRODUCT_TABLE = "DROP TABLE IF EXISTS Product"

        private const val SQL_CREATE_ORDER_PRODUCT_TABLE =
            "CREATE TABLE OrderProduct(" +
                    "OrderId INTEGER," +
                    "ProductId INTEGER," +
                    "Quantity INTEGER NOT NULL," +
                    "TotalPrice REAL NOT NULL," +
                    "FOREIGN KEY(ProductId) REFERENCES Product(IdProduct)," +
                    "PRIMARY KEY(OrderId, ProductId)" +
                    ")"
        private const val SQL_DROP_ORDER_PRODUCT_TABLE = "DROP TABLE IF EXISTS OrderProduct"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_PRODUCT_TABLE)
        db.execSQL(SQL_CREATE_ORDER_PRODUCT_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL(SQL_DROP_ORDER_PRODUCT_TABLE)
            db.execSQL(SQL_CREATE_ORDER_PRODUCT_TABLE)
        }
        db.execSQL(SQL_DROP_PRODUCT_TABLE)
        db.execSQL(SQL_CREATE_PRODUCT_TABLE)
    }

//    fun addOrderProduct(orderId: Int, productId: Int, quantity: Int) {
//        val db = writableDatabase
//        val product = getProductById(productId)
//        val totalPrice = product?.price?.toFloat()?.times(quantity) ?: 0f
//
//        val values = ContentValues().apply {
//            put("OrderId", orderId)
//            put("ProductId", productId)
//            put("Quantity", quantity)
//            put("TotalPrice", totalPrice)
//        }
//
//        db.insert("OrderProduct", null, values)
//        db.close()
//    }
//    fun getLastOrderId(): Int {
//        val db = readableDatabase
//        val cursor: Cursor = db.rawQuery("SELECT MAX(OrderId) FROM OrderProduct", null)
//        var lastOrderId = 0
//
//        if (cursor.moveToFirst()) {
//            lastOrderId = cursor.getInt(0)
//        }
//
//        cursor.close()
//        db.close()
//
//        return lastOrderId
//    }
    // Trong lớp DatabaseTACO

    fun deleteOrderProduct(orderId: Int, productId: Int) {
        val db = writableDatabase
        val whereClause = "orderId = ? AND productId = ?"
        val whereArgs = arrayOf(orderId.toString(), productId.toString())

        db.delete("OrderProduct", whereClause, whereArgs)
    }
    // Trong DatabaseTACO.kt
    fun deleteAllOrders() {
        val db = this.writableDatabase
        db.execSQL("DELETE FROM OrderProduct")
    }

    fun getOrderProducts(): List<OrderProduct> {
        val orderProducts = mutableListOf<OrderProduct>()
        val db = readableDatabase
        val query = """
        SELECT * FROM OrderProduct
        INNER JOIN Product ON OrderProduct.ProductId = Product.IdProduct
    """
        val cursor: Cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            do {
                val orderId = cursor.getInt(cursor.getColumnIndexOrThrow("OrderId"))
                val productId = cursor.getInt(cursor.getColumnIndexOrThrow("ProductId"))
                val quantity = cursor.getInt(cursor.getColumnIndexOrThrow("Quantity"))
                val totalPrice = cursor.getFloat(cursor.getColumnIndexOrThrow("TotalPrice"))

                // Fetch product details from the cursor
//                val name = cursor.getString(cursor.getColumnIndexOrThrow("Name"))
//                val price = cursor.getDouble(cursor.getColumnIndexOrThrow("Price"))
//                val oldPrice = cursor.getDoubleOrNull(cursor.getColumnIndexOrThrow("OldPrice"))
//                val image = cursor.getBlob(cursor.getColumnIndexOrThrow("Image"))

                // Create Product and OrderProduct objects
//                val product = Product(
//                    id = productId,
//                    name = name,
//                    price = price,
//                    oldPrice = oldPrice,
//                    image = image
//                )

                val orderProduct = OrderProduct(
                    orderId = orderId,
                    productId = productId,
                    quantity = quantity,
                    totalPrice = totalPrice
                )

                orderProducts.add(orderProduct)
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()

        return orderProducts
    }

//    fun getOrderProductsByOrderId(orderId: Int): List<OrderProduct> {
//        val orderProductList = mutableListOf<OrderProduct>()
//        val db = readableDatabase
//        val cursor: Cursor = db.rawQuery("SELECT * FROM OrderProduct WHERE OrderId = ?", arrayOf(orderId.toString()))
//
//        return if (cursor.moveToFirst()) {
//            val orderIdColumnIndex = cursor.getColumnIndex("OrderId")
//            val productIdColumnIndex = cursor.getColumnIndex("ProductId")
//            val quantityColumnIndex = cursor.getColumnIndex("Quantity")
//            val totalPriceColumnIndex = cursor.getColumnIndex("TotalPrice")
//
//            // Kiểm tra xem các cột có tồn tại không trước khi truy xuất
//            if (orderIdColumnIndex != -1 && productIdColumnIndex != -1 && quantityColumnIndex != -1 && totalPriceColumnIndex != -1) {
//                do {
//                    val orderProduct = OrderProduct(
//                        orderId = cursor.getInt(orderIdColumnIndex),
//                        productId = cursor.getInt(productIdColumnIndex),
//                        quantity = cursor.getInt(quantityColumnIndex),
//                        totalPrice = cursor.getFloat(totalPriceColumnIndex)
//                    )
//                    orderProductList.add(orderProduct)
//                } while (cursor.moveToNext())
//            }
//
//            cursor.close()
//            db.close()
//
//            orderProductList
//        } else {
//            cursor.close()
//            db.close()
//            emptyList() // Trả về danh sách rỗng nếu không có dữ liệu
//        }
//    }


//    fun deleteProductById(id: Int) {
//        val db = writableDatabase
//        db.delete("Product", "IdProduct=?", arrayOf(id.toString()))
//        db.close()
//    }
//    fun addProduct(name: String, price: Double, oldPrice: Double, image: ByteArray) {
//        val db = writableDatabase
//        val values = ContentValues().apply {
//            put("Name", name)
//            put("Price", price)
//            put("OldPrice", oldPrice)
//            put("Image", image)
//        }
//        db.insert("Product", null, values)
//    }
    @SuppressLint("Range")
    fun getAllProducts(): List<Product> {
        val productList = mutableListOf<Product>()
        val db = readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * FROM Product", null)

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndex("IdProduct"))
                val name = cursor.getString(cursor.getColumnIndex("Name"))
                val price = cursor.getDouble(cursor.getColumnIndex("Price"))
                val oldPrice = cursor.getDouble(cursor.getColumnIndex("OldPrice"))
                val image = cursor.getBlob(cursor.getColumnIndex("Image"))

                val product = Product(id, name, price, oldPrice, image)
                productList.add(product)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()

        return productList
    }
    fun getProductById(productId: Int): Product? {
        val db = readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * FROM Product WHERE IdProduct = ?", arrayOf(productId.toString()))

        return if (cursor.moveToFirst()) {
            val idColumnIndex = cursor.getColumnIndex("IdProduct")
            val nameColumnIndex = cursor.getColumnIndex("Name")
            val priceColumnIndex = cursor.getColumnIndex("Price")
            val oldPriceColumnIndex = cursor.getColumnIndex("OldPrice")
            val imageColumnIndex = cursor.getColumnIndex("Image")

            // Kiểm tra xem các cột có tồn tại không trước khi truy xuất
            if (idColumnIndex != -1 && nameColumnIndex != -1 && priceColumnIndex != -1) {
                val product = Product(
                    id = cursor.getInt(idColumnIndex),
                    name = cursor.getString(nameColumnIndex),
                    price = cursor.getDouble(priceColumnIndex),
                    oldPrice = if (oldPriceColumnIndex != -1) cursor.getDouble(oldPriceColumnIndex) else null,
                    image = if (imageColumnIndex != -1) cursor.getBlob(imageColumnIndex) else null
                )
                cursor.close()
                product
            } else {
                cursor.close()
                null
            }
        } else {
            cursor.close()
            null
        }
    }

//    fun updateProduct(productId: Int, name: String, price: Double, oldPrice: Double?) {
//        val db = writableDatabase
//        val values = ContentValues().apply {
//            put("Name", name)
//            put("Price", price)
//            put("OldPrice", oldPrice)
//        }
//        db.update("Product", values, "IdProduct = ?", arrayOf(productId.toString()))
//    }
//    fun getProductsByName(name: String): List<Product> {
//        val products = mutableListOf<Product>()
//        val db = this.readableDatabase
//        val cursor = db.rawQuery(
//            "SELECT * FROM Product WHERE Name LIKE ?",
//            arrayOf("%$name%")
//        )
//
//        if (cursor.moveToFirst()) {
//            val idIndex = cursor.getColumnIndex("IdProduct")
//            val nameIndex = cursor.getColumnIndex("Name")
//            val priceIndex = cursor.getColumnIndex("Price")
//            val oldPriceIndex = cursor.getColumnIndex("OldPrice")
//            val imageIndex = cursor.getColumnIndex("Image")
//
//            if (idIndex != -1 && nameIndex != -1 && priceIndex != -1 && oldPriceIndex != -1 && imageIndex != -1) {
//                do {
//                    val id = cursor.getInt(idIndex)
//                    val name = cursor.getString(nameIndex)
//                    val price = cursor.getFloat(priceIndex)
//                    val oldPrice = cursor.getFloat(oldPriceIndex)
//                    val image = cursor.getBlob(imageIndex)
//
//                    val product = Product(id, name, price.toDouble(), oldPrice.toDouble(), image)
//                    products.add(product)
//                } while (cursor.moveToNext())
//            }
//        }
//        cursor.close()
//        db.close()
//        return products
//    }

    data class Product(
        val id: Int,
        val name: String,
        val price: Double,
        val oldPrice: Double?,
        val image: ByteArray?
    )

    data class OrderProduct(
        val orderId: Int,
        val productId: Int,
        val quantity: Int,
        val totalPrice: Float
    )
}
