package lepus.mc.client.render;

import org.joml.Vector2f;
import org.joml.Vector3f;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;

import lepus.graphics.ColorRGBA;
import lepus.math.interpolation.ColorLinearInterpolation;
import lepus.mc.client.render.renderable.Texture;
import net.minecraft.util.Mth;

/**
 * 生成常用形状的顶点数据
 */
public class RenderableObjects {

	/**
	 * 生成纹理的矩形四边形
	 * 
	 * @param texture
	 * @param width
	 * @param z
	 * @param r
	 * @param g
	 * @param b
	 * @param a
	 * @return
	 */
	public static VanillaRenderable quad(Texture texture, float scale, float z, float r, float g, float b, float a) {
		float width = texture.width() * scale;
		float height = texture.height() * scale;
		VanillaRenderable obj = new VanillaRenderable(texture.location()).loadBuffer();
		obj.addVertex(-width / 2, -height / 2, z, texture.u1(), texture.v1(), r, g, b, a);
		obj.addVertex(-width / 2, height / 2, z, texture.u1(), texture.v2(), r, g, b, a);
		obj.addVertex(width / 2, -height / 2, z, texture.u2(), texture.v1(), r, g, b, a);
		obj.addVertex(width / 2, height / 2, z, texture.u2(), texture.v2(), r, g, b, a);
		return obj.flushBuffer();
	}

	public static VanillaRenderable quad(Texture texture, float scale, float z, float a) {
		return quad(texture, scale, z, 1.0f, 1.0f, 1.0f, a);
	}

	public static VanillaRenderable quad(Texture texture, float z, float a) {
		return quad(texture, 1, z, a);
	}

	public static VanillaRenderable quad(Texture texture, float z) {
		return quad(texture, z, 1.0f);
	}

	/**
	 * 构造与原版形状一致的天空穹，地平线以上和以下需要分开构造，渲染时使用的是两个不同天空穹
	 * 
	 * @param y
	 * @return
	 */
	public static VanillaRenderable sky(float y, float radius, float r, float g, float b, float a) {
		float x_signed_r = Math.signum(y) * radius;
		VanillaRenderable skyVertices = new VanillaRenderable(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR).loadBuffer();
		skyVertices.addVertex(0, y, 0, r, g, b, a);// 天顶
		for (int i = -180; i <= 180; i += 45) {
			skyVertices.addVertex(x_signed_r * Mth.cos(i * (float) (Math.PI / 180.0)), y, radius * Mth.sin(i * (float) (Math.PI / 180.0)), r, g, b, a);
		}
		return skyVertices.flushBuffer();
	}

	/**
	 * 纯白不透明的天空穹，需要使用RenderSystem.setShaderColor()设置其颜色和透明度
	 * 
	 * @param y
	 * @param radius
	 * @return
	 */
	public static VanillaRenderable sky(float y, float radius) {
		return sky(y, radius, 1, 1, 1, 1);
	}

	/**
	 * 地平线上的可见天空穹
	 * 
	 * @param r
	 * @param g
	 * @param b
	 * @param a
	 * @return
	 */
	public static VanillaRenderable skyAboveHorizonVanilla(float r, float g, float b, float a) {
		return sky(16, 512, r, g, b, a);
	}

	/**
	 * 地平线上的天空穹的缩放版本
	 * 
	 * @param scale
	 * @param r
	 * @param g
	 * @param b
	 * @param a
	 * @return
	 */
	public static VanillaRenderable scaledSkyAboveHorizonVanilla(float scale, float r, float g, float b, float a) {
		return sky(16 * scale, 512, r, g, b, a);
	}

	public static VanillaRenderable scaledSkyAboveHorizonVanilla(float scale) {
		return scaledSkyAboveHorizonVanilla(scale, 1, 1, 1, 1);
	}

	/**
	 * 地平线下的天空穹
	 * 
	 * @param r
	 * @param g
	 * @param b
	 * @param a
	 * @return
	 */
	public static VanillaRenderable skyBelowHorizonVanilla(float r, float g, float b, float a) {
		return sky(-16, 512, r, g, b, a);
	}

	/**
	 * 地平线下的天空穹的缩放版本
	 * 
	 * @param scale
	 * @param r
	 * @param g
	 * @param b
	 * @param a
	 * @return
	 */
	public static VanillaRenderable scaledSkyBelowHorizonVanilla(float scale, float r, float g, float b, float a) {
		return sky(-16 * scale, 512, r, g, b, a);
	}

	public static VanillaRenderable scaledSkyBelowHorizonVanilla(float scale) {
		return scaledSkyAboveHorizonVanilla(scale, 1, 1, 1, 1);
	}

	/**
	 * 生成水滴状带颜色顶点数据。<br>
	 * 其中一半为半球体，一半为圆锥体，原点位于球心。<br>
	 * 颜色为线性插值渐变
	 * 
	 * @param radius                 半球体半径
	 * @param tailLength             圆锥体长度
	 * @param latitudeBandsDivision  模型纬度细分精度
	 * @param longitudeBandsDivision 模型经度细分精度
	 * @param headColor              水滴头部顶点颜色
	 * @param junctionColor          水滴半球与圆锥连接处颜色
	 * @param tailColor              水滴尾部顶点颜色
	 * @return
	 */
	public static VanillaRenderable gradualColorDroplet(float radius, float tailLength, int latitudeBandsDivision, int longitudeBandsDivision, ColorRGBA headColor, ColorRGBA junctionColor, ColorRGBA tailColor) {
		float latitudeDivisionAngle = (float) (Math.PI / latitudeBandsDivision / 2);
		float longitudeDivisionAngle = (float) (Math.PI / longitudeBandsDivision * 2);
		Vector3f[][] sphereVertices = new Vector3f[latitudeBandsDivision + 1][longitudeBandsDivision + 1];// 纬度-经度索引
		ColorRGBA[][] colors = new ColorRGBA[latitudeBandsDivision + 1][longitudeBandsDivision + 1];
		for (int la = 0; la <= latitudeBandsDivision; ++la) {
			float latitude = latitudeDivisionAngle * la;// 从北天极向赤道计算角度，0-90
			float y = radius * (float) Math.cos(latitude);
			float sliceRadius = radius * (float) Math.sin(latitude);// 截面半径
			for (int lo = 0; lo <= longitudeBandsDivision; ++lo) {
				float longitude = longitudeDivisionAngle * lo;// 0-360
				float x = sliceRadius * (float) Math.cos(longitude);
				float z = sliceRadius * (float) Math.sin(longitude);
				sphereVertices[la][lo] = new Vector3f(x, y, z);
				colors[la][lo] = headColor.interplote(1 - y / radius, junctionColor);
			}
		}
		VanillaRenderable droplet = new VanillaRenderable(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR).loadBuffer();
		// 由于mc渲染系统的BufferBuilder构造的物体顶点EBO是完全没有体现出EBO意义的纯摆设且不支持自己写入索引，迫于与原版渲染系统保持兼容只能传入重复顶点
		for (int la = 0; la < latitudeBandsDivision; ++la) {
			for (int lo = 0; lo < longitudeBandsDivision; ++lo) {
				// 左上角，当前点
				Vector3f leftUp = sphereVertices[la][lo];
				ColorRGBA leftUpC = colors[la][lo];
				// 左下角，当前点正下方
				Vector3f leftDown = sphereVertices[la + 1][lo];
				ColorRGBA leftDownC = colors[la + 1][lo];
				// 右上角，当前点的正右方
				Vector3f rightUp = sphereVertices[la][lo + 1];
				ColorRGBA rightUpC = colors[la][lo + 1];
				// 右下角，当前点的正右下方
				Vector3f rightDown = sphereVertices[la + 1][lo + 1];
				ColorRGBA rightDownC = colors[la + 1][lo + 1];
				droplet.addVertex(leftUp.x, leftUp.y, leftUp.z, leftUpC.r, leftUpC.g, leftUpC.b, leftUpC.a);
				droplet.addVertex(leftDown.x, leftDown.y, leftDown.z, leftDownC.r, leftDownC.g, leftDownC.b, leftDownC.a);
				droplet.addVertex(rightUp.x, rightUp.y, rightUp.z, rightUpC.r, rightUpC.g, rightUpC.b, rightUpC.a);
				droplet.addVertex(rightUp.x, rightUp.y, rightUp.z, rightUpC.r, rightUpC.g, rightUpC.b, rightUpC.a);
				droplet.addVertex(leftDown.x, leftDown.y, leftDown.z, leftDownC.r, leftDownC.g, leftDownC.b, leftDownC.a);
				droplet.addVertex(rightDown.x, rightDown.y, rightDown.z, rightDownC.r, rightDownC.g, rightDownC.b, rightDownC.a);
			}
		}
		// 水滴尾部圆锥顶点生成，mc没有图元重启，导致上半球与下半圆锥有三角形连接
		for (int lo = 0; lo < longitudeBandsDivision; ++lo) {
			float longitude = longitudeDivisionAngle * lo;
			float x = radius * (float) Math.cos(longitude);
			float z = radius * (float) Math.sin(longitude);
			droplet.addVertex(x, 0, z, junctionColor.r, junctionColor.g, junctionColor.b, junctionColor.a);
			droplet.addVertex(0, -tailLength, 0, tailColor.r, tailColor.g, tailColor.b, tailColor.a);
			float next_theta = longitudeDivisionAngle * (lo + 1);
			float next_x = radius * (float) Math.cos(next_theta);
			float next_z = radius * (float) Math.sin(next_theta);
			droplet.addVertex(next_x, 0, next_z, junctionColor.r, junctionColor.g, junctionColor.b, junctionColor.a);
		}
		return droplet.flushBuffer();
	}

	public static VanillaRenderable gradualColorDroplet(float radius, float tailLength, int latitudeBandsDivision, int longitudeBandsDivision, ColorRGBA headColor, ColorRGBA tailColor) {
		return gradualColorDroplet(radius, tailLength, latitudeBandsDivision, longitudeBandsDivision, headColor, headColor, tailColor);
	}

	public static VanillaRenderable gradualColorDroplet(float radius, float tailLength, int division, ColorRGBA headColor, ColorRGBA tailColor) {
		return gradualColorDroplet(radius, tailLength, division, division, headColor, tailColor);
	}

	/**
	 * 带贴图的球体
	 * 
	 * @param radius
	 * @param latitudeBandsDivision
	 * @param longitudeBandsDivision
	 * @param invertNormal           是否反转正方向，默认球体外表面为正方向。此值关系到不可见面的剔除
	 * @return
	 */
	public static VanillaRenderable sphere(float radius, int latitudeBandsDivision, int longitudeBandsDivision, Texture texture, boolean invertNormal) {
		float latitudeDivisionAngle = (float) (Math.PI / latitudeBandsDivision);
		float longitudeDivisionAngle = (float) (Math.PI / longitudeBandsDivision * 2);
		float uDivision = (texture.u2() - texture.u1()) / longitudeBandsDivision;
		float vDivision = (texture.v2() - texture.v1()) / latitudeBandsDivision;
		Vector3f[][] sphereVertices = new Vector3f[latitudeBandsDivision + 1][longitudeBandsDivision + 1];// 纬度-经度索引
		Vector2f[][] uv = new Vector2f[latitudeBandsDivision + 1][longitudeBandsDivision + 1];
		for (int la = 0; la <= latitudeBandsDivision; ++la) {
			float latitude = latitudeDivisionAngle * la;// 从北天极向赤道计算角度，只算半球，角度0-180
			float y = radius * (float) Math.cos(latitude);
			float sliceRadius = radius * (float) Math.sin(latitude);// 截面半径
			for (int lo = 0; lo <= longitudeBandsDivision; ++lo) {
				float longitude = longitudeDivisionAngle * lo;// 经度，0-360
				float x = sliceRadius * (float) Math.cos(longitude);
				float z = sliceRadius * (float) Math.sin(longitude);
				sphereVertices[la][lo] = new Vector3f(x, y, z);
				uv[la][lo] = new Vector2f(texture.u1() + uDivision * lo, texture.v1() + 1.0f - vDivision * la);
			}
		}
		VanillaRenderable sphere = new VanillaRenderable(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_TEX, texture.location()).loadBuffer();
		for (int la = 0; la < latitudeBandsDivision; ++la) {
			for (int lo = 0; lo < longitudeBandsDivision; ++lo) {
				// 左上角，当前点
				Vector3f leftUp = sphereVertices[la][lo];
				Vector2f leftUpUV = uv[la][lo];
				// 左下角，当前点正下方
				Vector3f leftDown = sphereVertices[la + 1][lo];
				Vector2f leftDownUV = uv[la + 1][lo];
				// 右上角，当前点的正右方
				Vector3f rightUp = sphereVertices[la][lo + 1];
				Vector2f rightUpUV = uv[la][lo + 1];
				// 右下角，当前点的正右下方
				Vector3f rightDown = sphereVertices[la + 1][lo + 1];
				Vector2f rightDownUV = uv[la + 1][lo + 1];
				if (invertNormal) {
					sphere.addVertex(leftUp.x, leftUp.y, leftUp.z, leftUpUV.x, leftUpUV.y);
					sphere.addVertex(leftDown.x, leftDown.y, leftDown.z, leftDownUV.x, leftDownUV.y);
					sphere.addVertex(rightUp.x, rightUp.y, rightUp.z, rightUpUV.x, rightUpUV.y);
					sphere.addVertex(rightUp.x, rightUp.y, rightUp.z, rightUpUV.x, rightUpUV.y);
					sphere.addVertex(leftDown.x, leftDown.y, leftDown.z, leftDownUV.x, leftDownUV.y);
					sphere.addVertex(rightDown.x, rightDown.y, rightDown.z, rightDownUV.x, rightDownUV.y);
				} else {
					sphere.addVertex(leftUp.x, leftUp.y, leftUp.z, leftUpUV.x, leftUpUV.y);
					sphere.addVertex(rightUp.x, rightUp.y, rightUp.z, rightUpUV.x, rightUpUV.y);
					sphere.addVertex(leftDown.x, leftDown.y, leftDown.z, leftDownUV.x, leftDownUV.y);
					sphere.addVertex(leftDown.x, leftDown.y, leftDown.z, leftDownUV.x, leftDownUV.y);
					sphere.addVertex(rightUp.x, rightUp.y, rightUp.z, rightUpUV.x, rightUpUV.y);
					sphere.addVertex(rightDown.x, rightDown.y, rightDown.z, rightDownUV.x, rightDownUV.y);
				}
			}
		}
		return sphere.flushBuffer();
	}

	public static VanillaRenderable sphere(float radius, int latitudeBandsDivision, int longitudeBandsDivision, Texture texture) {
		return sphere(radius, latitudeBandsDivision, longitudeBandsDivision, texture, false);
	}

	/**
	 * 带颜色、贴图的球体
	 * 
	 * @param radius
	 * @param latitudeBandsDivision
	 * @param longitudeBandsDivision
	 * @param texture
	 * @param divisionColors         自变量取值-radius~radius，从南极到北极轴向线性插值颜色
	 * @param invertNormal
	 * @return
	 */
	public static VanillaRenderable coloredSphere(float radius, int latitudeBandsDivision, int longitudeBandsDivision, Texture texture, ColorLinearInterpolation divisionColors, boolean invertNormal) {
		float latitudeDivisionAngle = (float) (Math.PI / latitudeBandsDivision);
		float longitudeDivisionAngle = (float) (Math.PI / longitudeBandsDivision * 2);
		float uDivision = (texture.u2() - texture.u1()) / longitudeBandsDivision;
		float vDivision = (texture.v2() - texture.v1()) / latitudeBandsDivision;
		Vector3f[][] sphereVertices = new Vector3f[latitudeBandsDivision + 1][longitudeBandsDivision + 1];// 纬度-经度索引
		Vector2f[][] uv = new Vector2f[latitudeBandsDivision + 1][longitudeBandsDivision + 1];
		ColorRGBA[][] colors = new ColorRGBA[latitudeBandsDivision + 1][longitudeBandsDivision + 1];
		for (int la = 0; la <= latitudeBandsDivision; ++la) {
			float latitude = latitudeDivisionAngle * la;// 从北天极向赤道计算角度，只算半球，角度0-180
			float y = radius * (float) Math.cos(latitude);
			float sliceRadius = radius * (float) Math.sin(latitude);// 截面半径
			for (int lo = 0; lo <= longitudeBandsDivision; ++lo) {
				float longitude = longitudeDivisionAngle * lo;// 经度，0-360
				float x = sliceRadius * (float) Math.cos(longitude);
				float z = sliceRadius * (float) Math.sin(longitude);
				sphereVertices[la][lo] = new Vector3f(x, y, z);
				uv[la][lo] = new Vector2f(texture.u1() + uDivision * lo, texture.v1() + 1.0f - vDivision * la);
				colors[la][lo] = divisionColors.interploteColor(y);
			}
		}
		VanillaRenderable sphere = new VanillaRenderable(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_TEX, texture.location()).loadBuffer();
		for (int la = 0; la < latitudeBandsDivision; ++la) {
			for (int lo = 0; lo < longitudeBandsDivision; ++lo) {
				// 左上角，当前点
				Vector3f leftUp = sphereVertices[la][lo];
				Vector2f leftUpUV = uv[la][lo];
				ColorRGBA leftUpC = colors[la][lo];
				// 左下角，当前点正下方
				Vector3f leftDown = sphereVertices[la + 1][lo];
				Vector2f leftDownUV = uv[la + 1][lo];
				ColorRGBA leftDownC = colors[la + 1][lo];
				// 右上角，当前点的正右方
				Vector3f rightUp = sphereVertices[la][lo + 1];
				Vector2f rightUpUV = uv[la][lo + 1];
				ColorRGBA rightUpC = colors[la][lo + 1];
				// 右下角，当前点的正右下方
				Vector3f rightDown = sphereVertices[la + 1][lo + 1];
				Vector2f rightDownUV = uv[la + 1][lo + 1];
				ColorRGBA rightDownC = colors[la + 1][lo + 1];
				if (invertNormal) {
					sphere.addVertex(leftUp.x, leftUp.y, leftUp.z, leftUpUV.x, leftUpUV.y, leftUpC.r, leftUpC.g, leftUpC.b, leftUpC.a);
					sphere.addVertex(leftDown.x, leftDown.y, leftDown.z, leftDownUV.x, leftDownUV.y, leftDownC.r, leftDownC.g, leftDownC.b, leftDownC.a);
					sphere.addVertex(rightUp.x, rightUp.y, rightUp.z, rightUpUV.x, rightUpUV.y, rightUpC.r, rightUpC.g, rightUpC.b, rightUpC.a);
					sphere.addVertex(rightUp.x, rightUp.y, rightUp.z, rightUpUV.x, rightUpUV.y, rightUpC.r, rightUpC.g, rightUpC.b, rightUpC.a);
					sphere.addVertex(leftDown.x, leftDown.y, leftDown.z, leftDownUV.x, leftDownUV.y, leftDownC.r, leftDownC.g, leftDownC.b, leftDownC.a);
					sphere.addVertex(rightDown.x, rightDown.y, rightDown.z, rightDownUV.x, rightDownUV.y, rightDownC.r, rightDownC.g, rightDownC.b, rightDownC.a);
				} else {
					sphere.addVertex(leftUp.x, leftUp.y, leftUp.z, leftUpUV.x, leftUpUV.y, leftUpC.r, leftUpC.g, leftUpC.b, leftUpC.a);
					sphere.addVertex(rightUp.x, rightUp.y, rightUp.z, rightUpUV.x, rightUpUV.y, rightUpC.r, rightUpC.g, rightUpC.b, rightUpC.a);
					sphere.addVertex(leftDown.x, leftDown.y, leftDown.z, leftDownUV.x, leftDownUV.y, leftDownC.r, leftDownC.g, leftDownC.b, leftDownC.a);
					sphere.addVertex(leftDown.x, leftDown.y, leftDown.z, leftDownUV.x, leftDownUV.y, leftDownC.r, leftDownC.g, leftDownC.b, leftDownC.a);
					sphere.addVertex(rightUp.x, rightUp.y, rightUp.z, rightUpUV.x, rightUpUV.y, rightUpC.r, rightUpC.g, rightUpC.b, rightUpC.a);
					sphere.addVertex(rightDown.x, rightDown.y, rightDown.z, rightDownUV.x, rightDownUV.y, rightDownC.r, rightDownC.g, rightDownC.b, rightDownC.a);
				}
			}
		}
		return sphere.flushBuffer();
	}
}
