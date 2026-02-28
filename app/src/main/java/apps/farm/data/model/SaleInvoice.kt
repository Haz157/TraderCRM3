package apps.farm.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.*

@Entity(
    tableName = "saleinvoicetbl",
    foreignKeys = [
        ForeignKey(
            entity = Farm::class,
            parentColumns = ["id"],
            childColumns = ["farmId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = Cycle::class,
            parentColumns = ["id"],
            childColumns = ["cycleId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = Customer::class,
            parentColumns = ["id"],
            childColumns = ["customerId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = Safe::class,
            parentColumns = ["id"],
            childColumns = ["safeId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["farmId"]),
        Index(value = ["cycleId"]),
        Index(value = ["customerId"]),
        Index(value = ["safeId"])
    ]
)
data class SaleInvoice(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val farmId: String,
    val cycleId: String,
    val invoiceDate: Long,
    val customerId: String,
    val safeId: String,
    val receiveAmount: Double = 0.0,
    val discount: Double = 0.0,
    val addition: Double = 0.0,
    val totalEmptyWeight: Double = 0.0,
    val totalGrossWeight: Double = 0.0,
    val netWeight: Double = 0.0,
    val price: Double = 0.0,
    val totalPrice: Double = 0.0,
    val totalInvoice: Double = 0.0,
    val invoiceNo: Int = 0,
    val createdDate: Long = System.currentTimeMillis(),
    val isBlocked: Boolean = false
)

@Entity(tableName = "emptyweighttbl")
data class EmptyWeight(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val invoiceId: String,
    val weight: Double,
    val crateCount: Int
)

@Entity(tableName = "grossweighttbl")
data class GrossWeight(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val invoiceId: String,
    val weight: Double,
    val crateCount: Int
)
