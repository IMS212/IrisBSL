package net.coderbot.iris.postprocess;

import net.coderbot.iris.gl.IrisRenderSystem;
import net.coderbot.iris.gl.framebuffer.GlFramebuffer;
import net.coderbot.iris.rendertarget.RenderTargets;
import net.coderbot.iris.uniforms.FrameUpdateNotifier;
import net.coderbot.iris.uniforms.transforms.SmoothedFloat;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL21C;
import org.lwjgl.opengl.GL32C;

import java.nio.ByteBuffer;

public class CenterDepthSampler {
	private final SmoothedFloat centerDepthSmooth;
	private final GlFramebuffer depthBufferHolder;
	private final RenderTargets renderTargets;
	private boolean hasFirstSample;
	private boolean everRetrieved;
	private int[] pbos;
	private int index, nextIndex;

	public CenterDepthSampler(RenderTargets renderTargets, FrameUpdateNotifier updateNotifier) {
		// NB: This will always be one frame behind compared to the current frame.
		// That's probably for the best, since it can help avoid some pipeline stalls.
		// We're still going to get stalls, though.
		centerDepthSmooth = new SmoothedFloat(1.0f, 1.0f, this::sampleCenterDepth, updateNotifier);

		// Prior to OpenGL 4.1, all framebuffers must have at least 1 color target.
		depthBufferHolder = renderTargets.createFramebufferWritingToMain(new int[] {0});
		this.renderTargets = renderTargets;

		pbos = new int[2];
		GL32C.glGenBuffers(pbos);
		GL32C.glBindBuffer(GL32C.GL_PIXEL_PACK_BUFFER, pbos[0]);
		GL32C.glBufferData(GL32C.GL_PIXEL_PACK_BUFFER, 4, GL32C.GL_STREAM_READ);
		GL32C.glBindBuffer(GL32C.GL_PIXEL_PACK_BUFFER, pbos[1]);
		GL32C.glBufferData(GL32C.GL_PIXEL_PACK_BUFFER, 4, GL32C.GL_STREAM_READ);

		GL32C.glBindBuffer(GL32C.GL_PIXEL_PACK_BUFFER, 0);
	}

	private float sampleCenterDepth() {
		if (hasFirstSample && (!everRetrieved)) {
			// If the shaderpack isn't reading center depth values, don't bother sampling it
			// This improves performance with most shaderpacks
			return 0.0f;
		}

		hasFirstSample = true;

		index = (index + 1) % 2;
		nextIndex = (index + 1) % 2;

		this.depthBufferHolder.bind();

		float[] depthValue = new float[1];

		// copy pixels from framebuffer to PBO
		// Use offset instead of ponter.
		// OpenGL should perform asynch DMA transfer, so glReadPixels() will return immediately.
		GL32C.glBindBuffer(GL32C.GL_PIXEL_PACK_BUFFER, pbos[index]);

		// Read a single pixel from the depth buffer
		// TODO: glReadPixels forces a full pipeline stall / flush, and probably isn't too great for performance
		IrisRenderSystem.readPixels(
			renderTargets.getCurrentWidth() / 2, renderTargets.getCurrentHeight() / 2, 1, 1,
			GL11C.GL_DEPTH_COMPONENT, GL11C.GL_FLOAT, null
		);

		GL32C.glBindBuffer(GL32C.GL_PIXEL_PACK_BUFFER, pbos[nextIndex]);
		ByteBuffer src = GL32C.glMapBuffer(GL32C.GL_PIXEL_PACK_BUFFER, GL32C.GL_READ_ONLY);
		float result = 0;
		if(src != null)
		{
			result = src.getFloat();
			GL32C.glUnmapBuffer(GL32C.GL_PIXEL_PACK_BUFFER);        // release pointer to the mapped buffer
		}

		GL32C.glBindBuffer(GL32C.GL_PIXEL_PACK_BUFFER, 0);

		return result;
	}

	public float getCenterDepthSmoothSample() {
		everRetrieved = true;

		return centerDepthSmooth.getAsFloat();
	}
}
