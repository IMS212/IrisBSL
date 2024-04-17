package net.irisshaders.iris.shadows;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class ShadowMatrices {
	private static final float NEAR = 0.05f;
	private static final float FAR = 256.0f;

	// NB: These matrices are in column-major order, not row-major order like what you'd expect!

	public static Matrix4f createOrthoMatrix(float halfPlaneLength, float nearPlane, float farPlane) {
		return new Matrix4f(
			// column 1
			1.0f / halfPlaneLength, 0f, 0f, 0f,
			// column 2
			0f, 1.0f / halfPlaneLength, 0f, 0f,
			// column 3
			0f, 0f, 2.0f / (nearPlane - farPlane), 0f,
			// column 4
			0f, 0f, -(farPlane + nearPlane) / (farPlane - nearPlane), 1f
		);
	}

	public static Matrix4f createPerspectiveMatrix(float fov) {
		// This converts from degrees to radians.
		float yScale = (float) (1.0f / Math.tan(Math.toRadians(fov) * 0.5f));
		return new Matrix4f(
			// column 1
			yScale, 0f, 0f, 0f,
			// column 2
			0f, yScale, 0f, 0f,
			// column 3
			0f, 0f, (FAR + NEAR) / (NEAR - FAR), -1.0F,
			// column 4
			0f, 0f, 2.0F * FAR * NEAR / (NEAR - FAR), 1f
		);
	}

	public static void createBaselineModelViewMatrix(PoseStack target, float shadowAngle, float sunPathRotation) {
		float skyAngle;

		if (shadowAngle < 0.25f) {
			skyAngle = shadowAngle + 0.75f;
		} else {
			skyAngle = shadowAngle - 0.25f;
		}

		target.last().normal().identity();
		target.last().pose().identity();

		target.last().pose().translate(0.0f, 0.0f, -100.0f);
		target.mulPose(Axis.XP.rotationDegrees(90.0F));
		target.mulPose(Axis.ZP.rotationDegrees(skyAngle * -360.0f));
		target.mulPose(Axis.XP.rotationDegrees(sunPathRotation));
	}

	public static void snapModelViewToGrid(PoseStack target, float shadowIntervalSize, double cameraX, double cameraY, double cameraZ) {
		if (Math.abs(shadowIntervalSize) == 0.0F) {
			// Avoid a division by zero - semantically, this just means that the snapping does not take place,
			// if the shadow interval (size of each grid "cell") is zero.
			return;
		}

		// Calculate where we are within each grid "cell"
		// These values will be in the range of (-shadowIntervalSize, shadowIntervalSize)
		//
		// It looks like it's intended for these to be within the range [0, shadowIntervalSize), however since the
		// expression (-2.0f % 32.0f) returns -2.0f, negative inputs will result in negative outputs.
		float offsetX = (float) cameraX % shadowIntervalSize;
		float offsetY = (float) cameraY % shadowIntervalSize;
		float offsetZ = (float) cameraZ % shadowIntervalSize;

		// Halve the size of each grid cell in order to move to the center of it.
		float halfIntervalSize = shadowIntervalSize / 2.0f;

		// Shift by -halfIntervalSize
		//
		// It's clear that the intent of the algorithm was to place the values into the range:
		// [-shadowIntervalSize/2, shadowIntervalSize), however due to the previously-mentioned behavior with negatives,
		// it's possible that values will lie in the range (-3shadowIntervalSize/2, shadowIntervalSize/2).
		offsetX -= halfIntervalSize;
		offsetY -= halfIntervalSize;
		offsetZ -= halfIntervalSize;

		target.last().pose().translate(offsetX, offsetY, offsetZ);
	}

	public static void createModelViewMatrix(PoseStack target, float shadowAngle, float shadowIntervalSize,
											 float sunPathRotation, double cameraX, double cameraY, double cameraZ) {
		createBaselineModelViewMatrix(target, shadowAngle, sunPathRotation);
		snapModelViewToGrid(target, shadowIntervalSize, cameraX, cameraY, cameraZ);
	}

	private static final class Tests {
		public static void main(String[] args) {
			// const float shadowDistance = 32.0;
			// /* SHADOWHPL:32.0 */
			Matrix4f expected = new Matrix4f(
				0.03125f, 0f, 0f, 0f,
				0f, 0.03125f, 0f, 0f,
				0f, 0f, -0.007814026437699795f, 0f,
				0f, 0f, -1.000390648841858f, 1f
			);

			test("ortho projection hpl=32", expected, createOrthoMatrix(32.0f, 0.05f, 256.0f));

			// const float shadowDistance = 110.0;
			// /* SHADOWHPL:110.0 */
			Matrix4f expected110 = new Matrix4f(
				0.00909090880304575f, 0, 0, 0,
				0, 0.00909090880304575f, 0, 0,
				0, 0, -0.007814026437699795f, 0,
				0, 0, -1.000390648841858f, 1
			);

			test("ortho projection hpl=110", expected110, createOrthoMatrix(110.0f, 0.05f, 256.0f));

			Matrix4f expected90Proj = new Matrix4f(
				1.0f, 0.0f, 0.0f, 0.0f,
				0.0f, 1.0f, 0.0f, 0.0f,
				0.0f, 0.0f, -1.0003906f, -1.0f,
				0.0f, 0.0f, -0.10001954f, 0.0f
			);

			test("perspective projection fov=90", expected90Proj, createPerspectiveMatrix(90.0f));

			Matrix4f expectedModelViewAtDawn = new Matrix4f(
				// column 1
				0.21545040607452393f,
				5.820481518981069E-8f,
				0.9765146970748901f,
				0,
				// column 2
				-0.9765147466795349f,
				1.2841844920785661E-8f,
				0.21545039117336273f,
				0,
				// column 3
				0,
				-0.9999999403953552f,
				5.960464477539063E-8f,
				0,
				// column 4
				0.38002151250839233f,
				1.0264281034469604f,
				-100.4463119506836f,
				1
			);

			PoseStack modelView = new PoseStack();

			// NB: At dawn, the shadow angle is NOT zero.
			// When DayTime=0, skyAngle = 282 degrees.
			// Thus, sunAngle = shadowAngle = 0.03451777f
			createModelViewMatrix(modelView, 0.03451777f, 2.0f,
				0.0f, 0.646045982837677f, 82.53274536132812f, -514.0264282226562f);

			test("model view at dawn", expectedModelViewAtDawn, modelView.last().pose());
		}

		private static void test(String name, Matrix4f expected, Matrix4f created) {
			if (expected.equals(created, 0.0005f)) {
				System.err.println("test " + name + " failed: ");
				System.err.println("    expected: ");
				System.err.print(expected);
				System.err.println("    created: ");
				System.err.print(created.toString());
			} else {
				System.out.println("test " + name + " passed");
			}
		}
	}

	public static CascadeSplit[] updateCascadeShadows(Matrix4f viewMatrix, Matrix4f projMatrix, Vector3f lightDirection) {
		Vector4f lightPos = new Vector4f(lightDirection, 0);

		float cascadeSplitLambda = 0.95f;

		float[] cascadeSplits = new float[ShadowRenderer.CASCADE_COUNT];

		float nearClip = projMatrix.perspectiveNear();
		float farClip = projMatrix.perspectiveFar();
		float clipRange = farClip - nearClip;

		float minZ = nearClip;
		float maxZ = nearClip + clipRange;

		float range = maxZ - minZ;
		float ratio = maxZ / minZ;

		CascadeSplit[] splits = new CascadeSplit[ShadowRenderer.CASCADE_COUNT];

		// Based on https://developer.nvidia.com/gpugems/GPUGems3/gpugems3_ch10.html
		for (int i = 0; i < ShadowRenderer.CASCADE_COUNT; i++) {
			float p = (i + 1) / (float) (ShadowRenderer.CASCADE_COUNT);
			float log = (float) (minZ * java.lang.Math.pow(ratio, p));
			float uniform = minZ + range * p;
			float d = cascadeSplitLambda * (log - uniform) + uniform;
			cascadeSplits[i] = (d - nearClip) / clipRange;
		}

		// Calculate orthographic projection matrix for each cascade
		float lastSplitDist = 0.0f;
		for (int i = 0; i < ShadowRenderer.CASCADE_COUNT; i++) {
			float splitDist = cascadeSplits[i];

			Vector3f[] frustumCorners = new Vector3f[]{
				new Vector3f(-1.0f, 1.0f, -1.0f),
				new Vector3f(1.0f, 1.0f, -1.0f),
				new Vector3f(1.0f, -1.0f, -1.0f),
				new Vector3f(-1.0f, -1.0f, -1.0f),
				new Vector3f(-1.0f, 1.0f, 1.0f),
				new Vector3f(1.0f, 1.0f, 1.0f),
				new Vector3f(1.0f, -1.0f, 1.0f),
				new Vector3f(-1.0f, -1.0f, 1.0f),
			};

			// Project frustum corners into world space
			Matrix4f invCam = (new Matrix4f(projMatrix).mul(viewMatrix)).invert();
			for (int j = 0; j < 8; j++) {
				Vector4f invCorner = new Vector4f(frustumCorners[j], 1.0f).mul(invCam);
				frustumCorners[j] = new Vector3f(invCorner.x / invCorner.w, invCorner.y / invCorner.w, invCorner.z / invCorner.w);
			}

			for (int j = 0; j < 4; j++) {
				Vector3f dist = new Vector3f(frustumCorners[j + 4]).sub(frustumCorners[j]);
				frustumCorners[j + 4] = new Vector3f(frustumCorners[j]).add(new Vector3f(dist).mul(splitDist));
				frustumCorners[j] = new Vector3f(frustumCorners[j]).add(new Vector3f(dist).mul(lastSplitDist));
			}

			// Get frustum center
			Vector3f frustumCenter = new Vector3f(0.0f);
			for (int j = 0; j < 8; j++) {
				frustumCenter.add(frustumCorners[j]);
			}
			frustumCenter.div(8.0f);

			float radius = 0.0f;
			for (int j = 0; j < 8; j++) {
				float distance = (new Vector3f(frustumCorners[j]).sub(frustumCenter)).length();
				radius = java.lang.Math.max(radius, distance);
			}
			radius = (float) java.lang.Math.ceil(radius * 16.0f) / 16.0f;

			Vector3f maxExtents = new Vector3f(radius);
			Vector3f minExtents = new Vector3f(maxExtents).mul(-1);

			Vector3f lightDir = (new Vector3f(lightPos.x, lightPos.y, lightPos.z).mul(-1)).normalize();
			Vector3f eye = new Vector3f(frustumCenter).sub(new Vector3f(lightDir).mul(-minExtents.z));
			Vector3f up = new Vector3f(0.0f, 1.0f, 0.0f);
			Matrix4f lightViewMatrix = new Matrix4f().lookAt(eye, frustumCenter, up);
			Matrix4f lightOrthoMatrix = new Matrix4f().ortho
				(minExtents.x, maxExtents.x, minExtents.y, maxExtents.y, 0.0f, maxExtents.z - minExtents.z, true);

			// Store split distance and matrix in cascade
			float splitDistance = (nearClip + splitDist * clipRange) * -1.0f;

			splits[i] = new CascadeSplit(lightViewMatrix, lightOrthoMatrix, splitDistance);

			lastSplitDist = cascadeSplits[i];
		}

		return splits;
	}
}
