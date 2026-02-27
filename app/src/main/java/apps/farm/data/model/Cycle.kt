package apps.farm.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "Cycletbl",
    foreignKeys = [
        ForeignKey(
            entity = Farm::class,
            parentColumns = ["id"],
            childColumns = ["farmId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index(value = ["farmId"])]
)
data class Cycle(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val farmId: String,
    val cycleName: String,
    val sd: Long,
    val ed: Long,
    val isActive: Boolean = true
)
