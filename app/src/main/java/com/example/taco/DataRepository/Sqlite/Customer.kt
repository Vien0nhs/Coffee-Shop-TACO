    import android.content.Context
    import android.database.sqlite.SQLiteDatabase
    import android.database.sqlite.SQLiteOpenHelper
    import android.content.ContentValues

    data class Customers(
        val customerId: Int,
        val customerName: String,
        val customerNumPhone: String
    )

    class CustomerDatabase(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

        companion object {
            const val DATABASE_NAME = "customer.db"
            const val DATABASE_VERSION = 1

            const val TABLE_CUSTOMER = "Customer"
            const val COLUMN_CUSTOMER_ID = "CustomerId"
            const val COLUMN_CUSTOMER_NAME = "CustomerName"
            const val COLUMN_CUSTOMER_PHONE = "CustomerNumPhone"
        }

        override fun onCreate(db: SQLiteDatabase) {
            val createTable = """
                CREATE TABLE $TABLE_CUSTOMER (
                    $COLUMN_CUSTOMER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    $COLUMN_CUSTOMER_NAME TEXT NOT NULL,
                    $COLUMN_CUSTOMER_PHONE TEXT NOT NULL
                )
            """.trimIndent()
            db.execSQL(createTable)
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            db.execSQL("DROP TABLE IF EXISTS $TABLE_CUSTOMER")
            onCreate(db)
        }

        // Hàm thêm khách hàng vào database
        fun insertCustomer(customerName: String, customerNumPhone: String) {
            val db = writableDatabase

            val values = ContentValues().apply {
                put(COLUMN_CUSTOMER_NAME, customerName)
                put(COLUMN_CUSTOMER_PHONE, customerNumPhone)
            }

            db.insert(TABLE_CUSTOMER, null, values)
            db.close()
        }

        // Hàm lấy danh sách tất cả khách hàng
        fun getAllCustomers(): List<Customers> {
            val db = readableDatabase
            val customerList = mutableListOf<Customers>()

            val cursor = db.query(
                TABLE_CUSTOMER,  // Bảng cần query
                null,  // Lấy tất cả các cột
                null,  // Không có điều kiện WHERE
                null,  // Không có điều kiện WHERE
                null,  // Không group các hàng
                null,  // Không filter theo group
                null   // Không có thứ tự sắp xếp
            )

            with(cursor) {
                while (moveToNext()) {
                    val customerId = getInt(getColumnIndexOrThrow(COLUMN_CUSTOMER_ID))
                    val customerName = getString(getColumnIndexOrThrow(COLUMN_CUSTOMER_NAME))
                    val customerNumPhone = getString(getColumnIndexOrThrow(COLUMN_CUSTOMER_PHONE))
                    customerList.add(Customers(customerId, customerName, customerNumPhone))
                }
            }
            cursor.close()
            db.close()
            return customerList
        }

        // Hàm tìm khách hàng theo số điện thoại
        fun getCustomerByPhone(phoneNumber: String): Customers? {
            val db = readableDatabase
            val cursor = db.query(
                TABLE_CUSTOMER,
                null,
                "$COLUMN_CUSTOMER_PHONE = ?",
                arrayOf(phoneNumber),
                null,
                null,
                null
            )

            var customer: Customers? = null
            if (cursor.moveToFirst()) {
                val customerId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CUSTOMER_ID))
                val customerName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CUSTOMER_NAME))
                val customerNumPhone = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CUSTOMER_PHONE))
                customer = Customers(customerId, customerName, customerNumPhone)
            }

            cursor.close()
            db.close()
            return customer
        }
        // Hàm xóa tất cả khách hàng trong database
        fun deleteAllCustomers() {
            val db = writableDatabase
            db.delete(TABLE_CUSTOMER, null, null)  // Xóa toàn bộ các bản ghi trong bảng Customer
            db.close()
        }
        // Hàm cập nhật thông tin khách hàng trong database
        fun updateCustomer(customerId: Int, newCustomerName: String, newCustomerNumPhone: String): Int {
            val db = writableDatabase

            val values = ContentValues().apply {
                put(COLUMN_CUSTOMER_NAME, newCustomerName)
                put(COLUMN_CUSTOMER_PHONE, newCustomerNumPhone)
            }

            // Cập nhật thông tin khách hàng dựa trên ID của khách hàng
            val selection = "$COLUMN_CUSTOMER_ID = ?"
            val selectionArgs = arrayOf(customerId.toString())

            val count = db.update(
                TABLE_CUSTOMER,
                values,
                selection,
                selectionArgs
            )

            db.close()
            return count  // Trả về số hàng được cập nhật (nếu bằng 0, có nghĩa là không cập nhật được)
        }


    }
