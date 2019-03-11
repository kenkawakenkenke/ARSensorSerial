package com.ken.arsensorduino.util;

import com.google.ar.sceneform.rendering.Color;

public class ColorUtil {

  public static Color fromHue(float hue) {
    return fromHSV(hue, 1, 1);
  }

  // https://ja.wikipedia.org/wiki/HSV%E8%89%B2%E7%A9%BA%E9%96%93#%E3%82%BD%E3%83%95%E3%83%88%E3%82%A6%E3%82%A7%E3%82%A2%E3%81%A7%E3%81%AE%E5%A4%89%E6%8F%9B%E5%87%A6%E7%90%86_2
  public static Color fromHSV(float hue, float sat, float val) {
    if (sat == 0) {
      return toColor(val, val, val);
    }

    float h = (hue - (float)Math.floor(hue)) * 6f;
    float f = h - (float)Math.floor(h);
    float p = val * (1f - sat);
    float q = val * (1f - sat * f);
    float t = val * (1f - (sat * (1f - f)));
    switch ((int) h) {
      case 0:
        return toColor(val, t, p);
      case 1:
        return toColor(q, val, p);
      case 2:
        return toColor(p, val, t);
      case 3:
        return toColor(p, q, val);
      case 4:
        return toColor(t, p, val);
      case 5:
      default:
        return toColor(val, p, q);
    }
  }

  private static int toInt256(float v) {
    return (int) (v * 255f + 0.5f);
  }

  private static Color toColor(float r, float g, float b) {
    int rInt = toInt256(r);
    int gInt = toInt256(g);
    int bInt = toInt256(b);
    return new Color(0xff000000 | (rInt << 16) | (gInt << 8) | (bInt << 0));
  }
}
