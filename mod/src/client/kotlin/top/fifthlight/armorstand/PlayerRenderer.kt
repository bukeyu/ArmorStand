package top.fifthlight.armorstand

import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.render.entity.state.PlayerEntityRenderState
import net.minecraft.client.util.math.MatrixStack
import org.joml.Matrix4f
import top.fifthlight.armorstand.config.ConfigHolder
import top.fifthlight.blazerod.model.TaskMap
import top.fifthlight.armorstand.state.ModelInstanceManager
import top.fifthlight.blazerod.util.FramedObjectPool
import java.util.*

object PlayerRenderer {
    private var renderingWorld = false
    private val taskMap = TaskMap()

    fun startRenderWorld() {
        renderingWorld = true
    }

    private val matrix = Matrix4f()
    @JvmStatic
    fun appendPlayer(
        uuid: UUID,
        vanillaState: PlayerEntityRenderState,
        matrixStack: MatrixStack,
        light: Int,
    ): Boolean {
        val entry = ModelInstanceManager.get(uuid, System.nanoTime())
        if (entry !is ModelInstanceManager.ModelInstanceItem.Model) {
            return false
        }

        val controller = entry.controller
        val instance = entry.instance

        controller.update(uuid, vanillaState)
        controller.apply(instance)
        instance.update()

        val backupItem = matrixStack.peek().copy()
        matrixStack.pop()
        matrixStack.push()

        matrix.set(matrixStack.peek().positionMatrix)
        matrix.scale(ConfigHolder.config.value.modelScale.toFloat())
        matrix.mulLocal(RenderSystem.getModelViewStack())
        if (renderingWorld) {
            taskMap.addTask(instance.schedule(matrix, light))
        } else {
            instance.render(matrix, light)
        }

        matrixStack.pop()
        matrixStack.push()
        matrixStack.peek().apply {
            positionMatrix.set(backupItem.positionMatrix)
            normalMatrix.set(backupItem.normalMatrix)
        }
        return true
    }

    fun executeDraw() {
        renderingWorld = false
        taskMap.executeTasks()
    }
}
