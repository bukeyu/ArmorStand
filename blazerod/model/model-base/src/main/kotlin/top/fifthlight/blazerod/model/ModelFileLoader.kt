package top.fifthlight.blazerod.model

import top.fifthlight.blazerod.model.animation.Animation
import java.nio.ByteBuffer
import java.nio.file.Path

interface ModelFileLoader {
    enum class Ability {
        MODEL,
        EMBED_ANIMATION,
        EXTERNAL_ANIMATION,
        EMBED_THUMBNAIL,
    }

    val extensions: Map<String, Set<Ability>>
    val abilities: Set<Ability>
        get() = extensions.values.flatten().toSet()
    val probeLength: Int
    fun probe(buffer: ByteBuffer): Boolean

    fun load(path: Path, basePath: Path = path.parent): LoadResult

    data class LoadResult(
        val metadata: Metadata?,
        val model: Model? = null,
        val animations: List<Animation>?,
    )

    fun getThumbnail(path: Path, basePath: Path? = path.parent): ThumbnailResult = ThumbnailResult.Unsupported

    sealed class ThumbnailResult {
        object Unsupported : ThumbnailResult()

        object None : ThumbnailResult()

        data class Embed(
            val offset: Long,
            val length: Long,
            val type: Texture.TextureType? = null,
        ) : ThumbnailResult() {
            init {
                require(offset >= 0) { "Bad offset: $offset" }
                require(length >= 0) { "Bad length: $length" }
            }
        }
    }
}