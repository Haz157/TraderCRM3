package apps.farm.data.model

import androidx.room.Embedded

data class CustomerWithBalance(
    @Embedded val customer: Customer,
    val currentBalance: Double
)
