/*
 * Copyright (c) 2014-2025 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks.autofarm;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gl.GlUsage;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.wurstclient.util.RenderUtils;

public final class AutoFarmRenderer
{
	private VertexBuffer greenBuffer;
	private VertexBuffer cyanBuffer;
	private VertexBuffer redBuffer;
	
	public void reset()
	{
		Stream.of(greenBuffer, cyanBuffer, redBuffer).filter(Objects::nonNull)
			.forEach(VertexBuffer::close);
		greenBuffer = cyanBuffer = redBuffer = null;
	}
	
	public void render(MatrixStack matrixStack)
	{
		// GL settings
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		
		matrixStack.push();
		
		RenderUtils.applyRegionalRenderOffset(matrixStack);
		
		// RenderSystem.setShader(ShaderProgramKeys.POSITION);
		// Matrix4f viewMatrix = matrixStack.peek().getPositionMatrix();
		// Matrix4f projMatrix = RenderSystem.getProjectionMatrix();
		// ShaderProgram shader = RenderSystem.getShader();
		
		if(greenBuffer != null)
		{
			RenderSystem.setShaderColor(0, 1, 0, 0.5F);
			greenBuffer.bind();
			greenBuffer.draw(RenderLayer.getDebugQuads());
			VertexBuffer.unbind();
		}
		
		if(cyanBuffer != null)
		{
			RenderSystem.setShaderColor(0, 1, 1, 0.5F);
			cyanBuffer.bind();
			cyanBuffer.draw(RenderLayer.getDebugQuads());
			VertexBuffer.unbind();
		}
		
		if(redBuffer != null)
		{
			RenderSystem.setShaderColor(1, 0, 0, 0.5F);
			redBuffer.bind();
			redBuffer.draw(RenderLayer.getDebugQuads());
			VertexBuffer.unbind();
		}
		
		matrixStack.pop();
		
		// GL resets
		RenderSystem.setShaderColor(1, 1, 1, 1);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glDisable(GL11.GL_BLEND);
	}
	
	public void updateVertexBuffers(List<BlockPos> blocksToHarvest,
		Set<BlockPos> plants, List<BlockPos> blocksToReplant)
	{
		Tessellator tessellator = Tessellator.getInstance();
		Vec3d regionOffset = RenderUtils.getCameraRegion().negate().toVec3d();
		
		double boxMin = 1 / 16.0;
		double boxMax = 15 / 16.0;
		Box box = new Box(boxMin, boxMin, boxMin, boxMax, boxMax, boxMax);
		Box node = new Box(0.25, 0.25, 0.25, 0.75, 0.75, 0.75);
		
		updateGreenBuffer(blocksToHarvest, tessellator, box, regionOffset);
		updateCyanBuffer(plants, tessellator, node, regionOffset);
		updateRedBuffer(blocksToReplant, tessellator, box, regionOffset);
	}
	
	private void updateGreenBuffer(List<BlockPos> blocksToHarvest,
		Tessellator tessellator, Box box, Vec3d regionOffset)
	{
		if(greenBuffer != null)
		{
			greenBuffer.close();
			greenBuffer = null;
		}
		
		if(blocksToHarvest.isEmpty())
			return;
		
		greenBuffer = new VertexBuffer(GlUsage.STATIC_WRITE);
		BufferBuilder bufferBuilder = tessellator
			.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION);
		
		for(BlockPos pos : blocksToHarvest)
		{
			Box renderBox = box.offset(pos).offset(regionOffset);
			RenderUtils.drawOutlinedBox(renderBox, bufferBuilder);
		}
		
		BuiltBuffer buffer = bufferBuilder.end();
		greenBuffer.bind();
		greenBuffer.upload(buffer);
		VertexBuffer.unbind();
	}
	
	private void updateCyanBuffer(Set<BlockPos> plants, Tessellator tessellator,
		Box node, Vec3d regionOffset)
	{
		if(cyanBuffer != null)
		{
			cyanBuffer.close();
			cyanBuffer = null;
		}
		
		if(plants.isEmpty())
			return;
		
		cyanBuffer = new VertexBuffer(GlUsage.STATIC_WRITE);
		BufferBuilder bufferBuilder = tessellator
			.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION);
		
		for(BlockPos pos : plants)
		{
			Box renderNode = node.offset(pos).offset(regionOffset);
			RenderUtils.drawNode(renderNode, bufferBuilder);
		}
		
		BuiltBuffer buffer = bufferBuilder.end();
		cyanBuffer.bind();
		cyanBuffer.upload(buffer);
		VertexBuffer.unbind();
	}
	
	private void updateRedBuffer(List<BlockPos> blocksToReplant,
		Tessellator tessellator, Box box, Vec3d regionOffset)
	{
		if(redBuffer != null)
		{
			redBuffer.close();
			redBuffer = null;
		}
		
		if(blocksToReplant.isEmpty())
			return;
		
		redBuffer = new VertexBuffer(GlUsage.STATIC_WRITE);
		BufferBuilder bufferBuilder = tessellator
			.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION);
		
		for(BlockPos pos : blocksToReplant)
		{
			Box renderBox = box.offset(pos).offset(regionOffset);
			RenderUtils.drawOutlinedBox(renderBox, bufferBuilder);
		}
		
		BuiltBuffer buffer = bufferBuilder.end();
		redBuffer.bind();
		redBuffer.upload(buffer);
		VertexBuffer.unbind();
	}
}
