package top.fifthlight.blazerod.animation

import org.joml.Quaternionf
import org.joml.Vector3f
import top.fifthlight.blazerod.model.NodeTransform
import top.fifthlight.blazerod.model.RenderScene
import top.fifthlight.blazerod.model.animation.Animation
import top.fifthlight.blazerod.model.animation.AnimationChannel

object AnimationLoader {
    fun load(
        scene: RenderScene,
        animation: Animation,
        missingChannelHandler: (AnimationChannel<*>) -> Unit = {},
    ): AnimationItem {
        fun findTargetTransformIndex(channel: AnimationChannel<*>): Int? =
            channel.targetNode?.id.let { nodeId -> scene.nodeIdToTransformMap.getInt(nodeId).takeIf { it >= 0 } }
                ?: channel.targetNodeName?.let { name -> scene.nodeNameToTransformMap.getInt(name).takeIf { it >= 0 } }
                ?: channel.targetHumanoidTag?.let { humanoid ->
                    scene.humanoidTagToTransformMap.getInt(humanoid).takeIf { it >= 0 }
                }

        @Suppress("UNCHECKED_CAST")
        fun mapAnimationChannel(channel: AnimationChannel<*>): AnimationChannelItem<*>? {
            val index = findTargetTransformIndex(channel) ?: run {
                missingChannelHandler(channel)
                return null
            }
            return when (channel.type) {
                AnimationChannel.Type.RelativeNodeTransformItem -> AnimationChannelItem.RelativeNodeTransformItem(
                    index = index,
                    channel = channel as AnimationChannel<NodeTransform.Decomposed>,
                )

                AnimationChannel.Type.Translation -> AnimationChannelItem.TranslationItem(
                    index = index,
                    channel = channel as AnimationChannel<Vector3f>,
                )

                AnimationChannel.Type.Scale -> AnimationChannelItem.ScaleItem(
                    index = index,
                    channel = channel as AnimationChannel<Vector3f>,
                )

                AnimationChannel.Type.Rotation -> AnimationChannelItem.RotationItem(
                    index = index,
                    channel = channel as AnimationChannel<Quaternionf>,
                )
            }
        }

        return AnimationItem(
            name = animation.name,
            channels = animation.channels.mapNotNull(::mapAnimationChannel)
        )
    }
}