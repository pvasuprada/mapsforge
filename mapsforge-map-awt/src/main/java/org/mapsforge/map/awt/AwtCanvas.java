/*
 * Copyright 2010, 2011, 2012 mapsforge.org
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.mapsforge.map.awt;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.TexturePaint;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import org.mapsforge.core.graphics.Bitmap;
import org.mapsforge.core.graphics.Canvas;
import org.mapsforge.core.graphics.Cap;
import org.mapsforge.core.graphics.Matrix;
import org.mapsforge.core.graphics.Paint;
import org.mapsforge.core.graphics.Path;
import org.mapsforge.core.graphics.Style;

class AwtCanvas implements Canvas {
	private static int getCap(Cap cap) {
		switch (cap) {
			case BUTT:
				return BasicStroke.CAP_BUTT;
			case ROUND:
				return BasicStroke.CAP_ROUND;
			case SQUARE:
				return BasicStroke.CAP_SQUARE;
		}

		throw new IllegalArgumentException("unknown cap: " + cap);
	}

	private static Stroke getStroke(Paint paint) {
		int cap = getCap(paint.getStrokeCap());
		return new BasicStroke(paint.getStrokeWidth(), cap, BasicStroke.JOIN_ROUND);
	}

	private BufferedImage bufferedImage;
	private Graphics2D graphics2D;

	AwtCanvas() {
	}

	AwtCanvas(Graphics2D graphics2D) {
		this.graphics2D = graphics2D;
	}

	@Override
	public void drawBitmap(Bitmap bitmap, int left, int top) {
		this.graphics2D.drawImage(AwtGraphicFactory.getBufferedImage(bitmap), left, top, null);
	}

	@Override
	public void drawBitmap(Bitmap bitmap, Matrix matrix) {
		this.graphics2D.drawRenderedImage(AwtGraphicFactory.getBufferedImage(bitmap),
				AwtGraphicFactory.getAffineTransform(matrix));
	}

	@Override
	public void drawCircle(int x, int y, int radius, Paint paint) {
		setPaintAttributes(paint);
		this.graphics2D.drawOval(x, y, radius, radius);
	}

	@Override
	public void drawLine(int x1, int y1, int x2, int y2, Paint paint) {
		setPaintAttributes(paint);
		this.graphics2D.drawLine(x1, y1, x2, y2);
	}

	@Override
	public void drawPath(Path path, Paint paint) {
		setPaintAttributes(paint);

		AwtPaint awtPaint = AwtGraphicFactory.getAwtPaint(paint);
		if (awtPaint.bitmap != null) {
			Rectangle rectangle = new Rectangle(0, 0, awtPaint.bitmap.getWidth(), awtPaint.bitmap.getHeight());
			TexturePaint texturePaint = new TexturePaint(AwtGraphicFactory.getBufferedImage(awtPaint.bitmap), rectangle);
			this.graphics2D.setPaint(texturePaint);
		}

		AwtPath awtPath = AwtGraphicFactory.getAwtPath(path);
		Style style = awtPaint.style;
		switch (style) {
			case FILL:
				this.graphics2D.fill(awtPath.path2D);
				return;

			case STROKE:
				this.graphics2D.draw(awtPath.path2D);
				return;
		}

		throw new IllegalArgumentException("unknown style: " + style);
	}

	@Override
	public void drawText(String text, int x, int y, Paint paint) {
		setPaintAttributes(paint);
		this.graphics2D.setFont(AwtGraphicFactory.getAwtPaint(paint).font);
		this.graphics2D.drawString(text, x, y);
	}

	@Override
	public void drawTextRotated(String text, int x1, int y1, int x2, int y2, Paint paint) {
		AffineTransform affineTransform = this.graphics2D.getTransform();

		double theta = Math.atan2(y2 - y1, x2 - x1);
		this.graphics2D.rotate(theta, x1, y1);

		double lineLength = Math.hypot(x2 - x1, y2 - y1);
		int textWidth = paint.getTextWidth(text);
		int dx = (int) (lineLength - textWidth) / 2;
		int xy = paint.getTextHeight(text) / 3;
		drawText(text, x1 + dx, y1 + xy, paint);

		this.graphics2D.setTransform(affineTransform);
	}

	@Override
	public void fillColor(int color) {
		this.graphics2D.setColor(new java.awt.Color(color));
		this.graphics2D.fillRect(0, 0, getWidth(), getHeight());
	}

	@Override
	public int getHeight() {
		return this.bufferedImage.getHeight();
	}

	@Override
	public int getWidth() {
		return this.bufferedImage.getWidth();
	}

	@Override
	public void setBitmap(Bitmap bitmap) {
		if (bitmap == null) {
			this.bufferedImage = null;
			this.graphics2D = null;
		} else {
			this.bufferedImage = AwtGraphicFactory.getBufferedImage(bitmap);
			this.graphics2D = this.bufferedImage.createGraphics();
			this.graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			this.graphics2D.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		}
	}

	private void setPaintAttributes(Paint paint) {
		this.graphics2D.setColor(new java.awt.Color(paint.getColor()));
		this.graphics2D.setStroke(getStroke(paint));
	}
}
