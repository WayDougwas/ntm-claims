import javax.imageio.ImageIO; import java.awt.*; import java.awt.geom.*; import java.awt.image.BufferedImage; import java.io.File;
public class LogoShield { public static void main(String[] a) throws Exception {
  BufferedImage src = ImageIO.read(new File(a[0]));
  int N = 512; double k = N / 1024.0;
  BufferedImage img = new BufferedImage(N, N, BufferedImage.TYPE_INT_ARGB);
  Graphics2D g = img.createGraphics();
  g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
  g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
  g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
  g.drawImage(src, 0, 0, N, N, null);
  // circle centre in the original: (512, 708), radius ~58
  double cx = 512 * k, cy = 708 * k, r = 52 * k;
  Color yellow = new Color(0xFFD21E);
  // soft glow behind the trefoil
  for (int i = 12; i > 0; i--) {
    float al = 0.035f * (13 - i);
    g.setColor(new Color(1f, 0.82f, 0.12f, Math.min(1f, al * 0.6f)));
    double gr = r + i * 4 * k;
    g.fill(new Ellipse2D.Double(cx - gr, cy - gr, gr * 2, gr * 2));
  }
  g.setColor(new Color(0x0B0E17));
  g.fill(new Ellipse2D.Double(cx - r, cy - r, r * 2, r * 2));
  // radiation trefoil
  g.setColor(yellow);
  double ro = r * 0.92, ri = r * 0.30, hub = r * 0.18;
  for (int i = 0; i < 3; i++) {
    double start = 90 + i * 120 - 30; // blades centred at 90, 210, 330 (one pointing up)
    Area blade = new Area(new Arc2D.Double(cx - ro, cy - ro, ro * 2, ro * 2, start, 60, Arc2D.PIE));
    blade.subtract(new Area(new Ellipse2D.Double(cx - ri, cy - ri, ri * 2, ri * 2)));
    g.fill(blade);
  }
  g.fill(new Ellipse2D.Double(cx - hub, cy - hub, hub * 2, hub * 2));
  // keep the white ring of the original logo around it
  g.setColor(Color.WHITE);
  g.setStroke(new BasicStroke((float) (12 * k)));
  double rr = 58 * k;
  g.draw(new Ellipse2D.Double(cx - rr, cy - rr, rr * 2, rr * 2));
  // shield badge (claim protection) in the top-right corner
  double bx = 800 * k, by = 224 * k, w = 150 * k, h = 176 * k;
  Path2D sh = new Path2D.Double();
  sh.moveTo(bx, by - h / 2);
  sh.curveTo(bx + w * 0.30, by - h * 0.40, bx + w * 0.45, by - h * 0.42, bx + w / 2, by - h * 0.36);
  sh.curveTo(bx + w / 2, by + h * 0.05, bx + w * 0.38, by + h * 0.33, bx, by + h / 2);
  sh.curveTo(bx - w * 0.38, by + h * 0.33, bx - w / 2, by + h * 0.05, bx - w / 2, by - h * 0.36);
  sh.curveTo(bx - w * 0.45, by - h * 0.42, bx - w * 0.30, by - h * 0.40, bx, by - h / 2);
  sh.closePath();
  g.setColor(new Color(0x0B0E17));
  g.setStroke(new BasicStroke((float) (16 * k), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
  g.draw(sh);
  g.setColor(yellow);
  g.fill(sh);
  // check mark
  g.setColor(new Color(0x0B0E17));
  g.setStroke(new BasicStroke((float) (22 * k), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
  Path2D ck = new Path2D.Double();
  ck.moveTo(bx - w * 0.24, by + h * 0.00);
  ck.lineTo(bx - w * 0.04, by + h * 0.17);
  ck.lineTo(bx + w * 0.26, by - h * 0.15);
  g.draw(ck);
  g.dispose();
  ImageIO.write(img, "png", new File(a[1]));
} }
