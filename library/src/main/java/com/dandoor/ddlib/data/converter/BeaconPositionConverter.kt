import androidx.room.TypeConverter
import com.dandoor.ddlib.data.entity.BeaconPosition
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class BeaconPositionConverter {
    @TypeConverter
    fun fromBeaconPositionList(value: List<BeaconPosition>): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toBeaconPositionList(value: String): List<BeaconPosition> {
        val listType = object : TypeToken<List<BeaconPosition>>() {}.type
        return Gson().fromJson(value, listType)
    }
}