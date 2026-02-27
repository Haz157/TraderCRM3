package apps.farm.ui.theme.icons

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.graphics.StrokeJoin.Companion.Miter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

public val Icons.Farm: ImageVector
    get() {
        if (_farm != null) {
            return _farm!!
        }
        _farm =
            Builder(
                    name = "Farm",
                    defaultWidth = 800.0.dp,
                    defaultHeight = 800.0.dp,
                    viewportWidth = 14.0f,
                    viewportHeight = 14.0f,
                )
                .apply {
                    path(
                        fill = SolidColor(Color(0xFF000000)),
                        stroke = null,
                        strokeLineWidth = 0.0f,
                        strokeLineCap = Butt,
                        strokeLineJoin = Miter,
                        strokeLineMiter = 4.0f,
                        pathFillType = NonZero,
                    ) {
                        moveToRelative(3.038f, 1.0f)
                        curveToRelative(-0.067f, 0.0f, -0.137f, 0.022f, -0.188f, 0.073f)
                        lineTo(1.595f, 2.329f)
                        curveToRelative(-0.102f, 0.102f, -0.102f, 0.265f, 0.0f, 0.367f)
                        lineToRelative(1.003f, 0.995f)
                        arcToRelative(0.262f, 0.262f, 0.0f, false, false, 0.09f, 0.09f)
                        lineTo(4.155f, 5.255f)
                        curveTo(4.192f, 5.098f, 4.276f, 4.956f, 4.399f, 4.848f)
                        lineTo(4.44f, 4.807f)
                        lineTo(4.236f, 4.603f)
                        lineTo(5.125f, 3.715f)
                        lineTo(5.386f, 3.984f)
                        lineTo(5.777f, 3.633f)
                        lineTo(4.302f, 2.158f)
                        arcToRelative(0.261f, 0.261f, 0.0f, false, false, -0.033f, -0.041f)
                        lineToRelative(-0.024f, -0.016f)
                        arcTo(0.261f, 0.261f, 0.0f, false, false, 4.204f, 2.068f)
                        lineTo(3.217f, 1.073f)
                        curveTo(3.166f, 1.022f, 3.105f, 1.0f, 3.038f, 1.0f)
                        close()
                        moveTo(10.962f, 1.0f)
                        curveToRelative(-0.067f, 0.0f, -0.128f, 0.022f, -0.179f, 0.073f)
                        lineTo(9.755f, 2.101f)
                        arcToRelative(0.261f, 0.261f, 0.0f, false, false, -0.073f, 0.073f)
                        lineTo(8.223f, 3.633f)
                        lineTo(8.614f, 3.984f)
                        lineTo(8.875f, 3.715f)
                        lineTo(9.764f, 4.603f)
                        lineTo(9.56f, 4.807f)
                        lineTo(9.601f, 4.848f)
                        curveTo(9.724f, 4.956f, 9.808f, 5.098f, 9.845f, 5.255f)
                        lineTo(11.304f, 3.788f)
                        arcToRelative(0.262f, 0.262f, 0.0f, false, false, 0.09f, -0.09f)
                        lineToRelative(1.011f, -1.003f)
                        curveToRelative(0.102f, -0.102f, 0.102f, -0.265f, 0.0f, -0.367f)
                        lineTo(11.149f, 1.073f)
                        curveTo(11.098f, 1.022f, 11.029f, 1.0f, 10.962f, 1.0f)
                        close()
                        moveTo(3.038f, 1.628f)
                        lineTo(3.707f, 2.304f)
                        lineTo(2.826f, 3.185f)
                        lineTo(2.149f, 2.516f)
                        lineTo(3.038f, 1.628f)
                        close()
                        moveTo(10.962f, 1.628f)
                        lineTo(11.851f, 2.516f)
                        lineTo(11.174f, 3.185f)
                        lineTo(10.293f, 2.304f)
                        lineTo(10.962f, 1.628f)
                        close()
                        moveTo(4.082f, 2.671f)
                        lineTo(4.75f, 3.348f)
                        lineTo(3.87f, 4.228f)
                        lineTo(3.193f, 3.56f)
                        lineTo(4.082f, 2.671f)
                        close()
                        moveTo(9.918f, 2.671f)
                        lineTo(10.807f, 3.56f)
                        lineTo(10.13f, 4.228f)
                        lineTo(9.25f, 3.348f)
                        lineTo(9.918f, 2.671f)
                        close()
                        moveTo(7.0f, 3.348f)
                        curveToRelative(-0.061f, 0.0f, -0.122f, 0.022f, -0.171f, 0.065f)
                        lineToRelative(-2.014f, 1.761f)
                        lineToRelative(4.37f, 0.0f)
                        lineTo(7.171f, 3.413f)
                        curveTo(7.122f, 3.37f, 7.061f, 3.348f, 7.0f, 3.348f)
                        close()
                        moveTo(4.636f, 5.696f)
                        lineTo(4.13f, 12.723f)
                        curveToRelative(-0.005f, 0.072f, 0.024f, 0.143f, 0.073f, 0.196f)
                        curveTo(4.253f, 12.971f, 4.319f, 13.0f, 4.391f, 13.0f)
                        lineTo(9.609f, 13.0f)
                        curveToRelative(0.072f, 0.0f, 0.138f, -0.029f, 0.188f, -0.082f)
                        curveToRelative(0.049f, -0.053f, 0.079f, -0.123f, 0.073f, -0.196f)
                        lineToRelative(-0.505f, -7.027f)
                        lineToRelative(-4.728f, 0.0f)
                        close()
                        moveTo(6.739f, 7.0f)
                        lineToRelative(0.522f, 0.0f)
                        lineToRelative(0.0f, 0.783f)
                        lineToRelative(-0.522f, 0.0f)
                        lineToRelative(0.0f, -0.783f)
                        close()
                        moveTo(3.951f, 7.905f)
                        lineTo(1.595f, 10.261f)
                        curveToRelative(-0.102f, 0.102f, -0.102f, 0.265f, 0.0f, 0.367f)
                        lineToRelative(1.255f, 1.255f)
                        curveToRelative(0.051f, 0.051f, 0.121f, 0.073f, 0.188f, 0.073f)
                        curveToRelative(0.067f, 0.0f, 0.128f, -0.023f, 0.179f, -0.073f)
                        lineToRelative(0.489f, -0.489f)
                        lineToRelative(0.098f, -1.386f)
                        lineToRelative(-0.611f, -0.611f)
                        lineToRelative(0.677f, -0.668f)
                        lineToRelative(0.024f, 0.016f)
                        lineToRelative(0.057f, -0.84f)
                        close()
                        moveTo(10.049f, 7.905f)
                        lineTo(10.106f, 8.745f)
                        lineTo(10.13f, 8.728f)
                        lineTo(10.807f, 9.397f)
                        lineTo(10.196f, 10.008f)
                        lineTo(10.293f, 11.394f)
                        lineTo(10.783f, 11.883f)
                        curveToRelative(0.051f, 0.051f, 0.113f, 0.073f, 0.179f, 0.073f)
                        curveToRelative(0.067f, 0.0f, 0.137f, -0.023f, 0.188f, -0.073f)
                        lineToRelative(1.255f, -1.255f)
                        curveToRelative(0.102f, -0.102f, 0.102f, -0.265f, 0.0f, -0.367f)
                        lineTo(10.049f, 7.905f)
                        close()
                        moveTo(2.826f, 9.772f)
                        lineTo(3.707f, 10.652f)
                        lineTo(3.038f, 11.329f)
                        lineTo(2.149f, 10.44f)
                        lineTo(2.826f, 9.772f)
                        close()
                        moveTo(11.174f, 9.772f)
                        lineTo(11.851f, 10.44f)
                        lineTo(10.962f, 11.329f)
                        lineTo(10.293f, 10.652f)
                        lineTo(11.174f, 9.772f)
                        close()
                        moveTo(7.0f, 10.391f)
                        curveToRelative(0.432f, 0.0f, 0.783f, 0.35f, 0.783f, 0.783f)
                        lineToRelative(0.0f, 1.304f)
                        lineToRelative(-1.565f, 0.0f)
                        lineToRelative(0.0f, -1.304f)
                        curveToRelative(0.0f, -0.432f, 0.35f, -0.783f, 0.783f, -0.783f)
                        close()
                    }
                }
                .build()
        return _farm!!
    }

private var _farm: ImageVector? = null

@Preview
@Composable
private fun Preview() {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = Icons.Farm, contentDescription = null)
    }
}
