package simple

import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.example.customizableseekbar.R
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val view = View(this).apply {
            id = View.generateViewId()
            layoutParams = ConstraintLayout.LayoutParams(50, 50)
            setBackgroundColor(ContextCompat.getColor(this@MainActivity, R.color.colorPrimary))
            parent_view.addView(this)
        }

        seek_bar.addUpdateListener(
                onStartTrackingTouch = {
                    view.setLayerType(View.LAYER_TYPE_HARDWARE, null)
                },
                onStopTrackingTouch = {
                    view.setLayerType(View.LAYER_TYPE_NONE, null)
                },
                onProgressChangedListener = { centerX, centerY, width, height, progress ->
                    text.text = progress.toString()
                    view.x = (((progress * (seek_bar.width - (seek_bar.paddingStart + seek_bar.paddingEnd))) / seek_bar.maxProgress) + seek_bar.paddingStart) - view.width.toFloat() / 2
                    view.y = centerY.toFloat() - width / 2
                }
        )
    }
}
