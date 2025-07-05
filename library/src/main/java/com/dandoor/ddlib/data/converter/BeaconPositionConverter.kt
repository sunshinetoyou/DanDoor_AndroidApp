import androidx.room.TypeConverter
import com.dandoor.ddlib.data.entity.Position
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class BeaconPositionConverter {
    @TypeConverter
    fun fromBeaconPositionList(value: List<Position>): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toBeaconPositionList(value: String): List<Position> {
        val listType = object : TypeToken<List<Position>>() {}.type
        return Gson().fromJson(value, listType)
    }
}