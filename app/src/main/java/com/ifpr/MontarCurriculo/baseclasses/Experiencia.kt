import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Experiencia(
    var empresa: String? = null,
    var cargo: String? = null,
    var periodo: String? = null,
    var descricao: String? = null,
    var principaisAtividades: List<String>? = null
) : Parcelable
