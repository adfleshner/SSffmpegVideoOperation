package com.simform.videoimageeditor.videoProcessActivity

import android.annotation.SuppressLint
import android.view.View
import android.widget.Toast
import com.arthenica.mobileffmpeg.LogMessage
import com.jaiselrahman.filepicker.model.MediaFile
import com.simform.videoimageeditor.BaseActivity
import com.simform.videoimageeditor.R
import com.simform.videoimageeditor.utility.Common
import com.simform.videoimageeditor.utility.Common.RATIO_1
import com.simform.videoimageeditor.utility.FFmpegCallBack
import com.simform.videoimageeditor.utility.FFmpegQueryExtension
import java.util.concurrent.CyclicBarrier
import kotlinx.android.synthetic.main.activity_aspect_ratio.btnAspectRatio
import kotlinx.android.synthetic.main.activity_aspect_ratio.btnVideoPath
import kotlinx.android.synthetic.main.activity_aspect_ratio.mProgressView
import kotlinx.android.synthetic.main.activity_aspect_ratio.tvInputPathVideo
import kotlinx.android.synthetic.main.activity_aspect_ratio.tvOutputPath

class AspectRatioActivity : BaseActivity(R.layout.activity_aspect_ratio, R.string.apply_aspect_ratio) {
    private var isInputVideoSelected: Boolean = false
    override fun initialization() {
        btnVideoPath.setOnClickListener(this)
        btnAspectRatio.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnVideoPath -> {
                Common.selectFile(this, maxSelection = 1, isImageSelection = false, isAudioSelection = false)
            }
            R.id.btnAspectRatio -> {
                when {
                    !isInputVideoSelected -> {
                        Toast.makeText(this, getString(R.string.input_video_validate_message), Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        processStart()
                        val gate = CyclicBarrier(2)
                        object : Thread() {
                            override fun run() {
                                gate.await()
                                applyRatioProcess()
                            }
                        }.start()
                        gate.await()
                    }
                }
            }
        }
    }

    private fun applyRatioProcess() {
        val outputPath = Common.getFilePath(this, Common.VIDEO)
        val query = FFmpegQueryExtension.applyRatio(tvInputPathVideo.text.toString(),RATIO_1, outputPath)

        Common.callQuery(this, query, object : FFmpegCallBack {
            override fun process(logMessage: LogMessage) {
                tvOutputPath.text = logMessage.text
            }

            override fun success() {
                tvOutputPath.text = "Output Path : \n$outputPath"
                processStop()
            }

            override fun cancel() {
                processStop()
            }

            override fun failed() {
                processStop()
            }

        })
    }

    @SuppressLint("NewApi")
    override fun selectedFiles(mediaFiles: List<MediaFile>?, requestCode: Int) {
        when (requestCode) {
            Common.VIDEO_FILE_REQUEST_CODE -> {
                if (mediaFiles != null && mediaFiles.isNotEmpty()) {
                    tvInputPathVideo.text = mediaFiles[0].path
                    isInputVideoSelected = true
                } else {
                    Toast.makeText(this, getString(R.string.video_not_selected_toast_message), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun processStop() {
        runOnUiThread {
            btnVideoPath.isEnabled = true
            btnAspectRatio.isEnabled = true
            mProgressView.visibility = View.GONE
        }
    }

    private fun processStart() {
        btnVideoPath.isEnabled = false
        btnAspectRatio.isEnabled = false
        mProgressView.visibility = View.VISIBLE
    }

}