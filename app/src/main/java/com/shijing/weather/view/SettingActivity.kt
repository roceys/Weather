package com.shijing.weather.view

import android.Manifest
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import com.hjq.toast.ToastUtils
import com.june.basicslibrary.base.BaseActivity
import com.june.basicslibrary.dialog.AwaitDialog0
import com.june.basicslibrary.dialog.UpdateDialog
import com.june.basicslibrary.intefaceA.UpdateDialogInterface
import com.june.basicslibrary.utils.*
import com.permissionx.guolindev.PermissionX
import com.shijing.weather.BuildConfig
import com.shijing.weather.R
import com.shijing.weather.data.storage.WeatherDataKv
import com.shijing.weather.utils.WeatherUtils
import com.shijing.weather.viewModel.SettingViewModel
import com.zaaach.citypicker.adapter.OnPickListener
import com.zaaach.citypicker.model.City
import kotlinx.android.synthetic.main.activity_setting.*

class SettingActivity : BaseActivity() {
    override var barColor: Int = Color.parseColor("#EFEDED")

    val viewModel by viewModels<SettingViewModel>()
    //是否拥有定位权限
    var permission = Common.checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    private lateinit var awaitDialog: AwaitDialog0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        initView()
        startObserve()
    }

    private fun startObserve() {
        //手动切换城市
        viewModel.cityName.observe(this, {
            setting_cityName.setTv1Text(it)
        })

        //获取到OSS版本信息
        viewModel.debugVersion.observe(this,{
            awaitDialog.dismiss()

            if (it.versionCode!!.toInt() > BuildConfig.VERSION_CODE) {
                val updateDialog = UpdateDialog(this)

                updateDialog.setClickListener(object : UpdateDialogInterface {
                    override fun confirm() {
                        //判断文件是否已经存在 -> 已存在直接安装
                        if (FileUtils.checkFileExist(DownloadManagerUtils.apkPathUrl)) {
                            DownloadManagerUtils.installAPK(this@SettingActivity)
                        } else {
                            //叫外卖
                            val url = BuildConfig.OssBasePath+ it.filePath
                            DownloadManagerUtils.createDownloadService(this@SettingActivity,
                                url,DownloadManagerUtils.localFileDir,DownloadManagerUtils.localFileName,
                                broadcastReceiver = object : BroadcastReceiver() {
                                    override fun onReceive(context: Context?, intent: Intent?) {
                                        if (DownloadManagerUtils.referenceId == intent!!.getLongExtra(
                                                DownloadManager.EXTRA_DOWNLOAD_ID,
                                                -1
                                            )
                                        ) {
                                            //检查下载完整，取消了下载的话不弹出
                                            if (FileUtils.checkFileExist(DownloadManagerUtils.apkPathUrl)) {
                                                DownloadManagerUtils.installAPK(this@SettingActivity)
                                            }

                                        }
                                    }
                                })
                            UtilToast.show("开始下载")
                        }
                        updateDialog.dismiss()
                    }

                    override fun cancel() {
                        updateDialog.dismiss()
                    }
                })

                updateDialog.show()
            } else {
                UtilToast.show("已是最新版本")
            }
        })

    }

    private fun initView() {

        if(!permission){
            settingGETLocation.visibility = View.VISIBLE
        }

        settingGETLocation.setOnClickListener {
            initPermissionX()
        }

        //检查更新
        settingUpdate.setOnClickListener {
            awaitDialog = AwaitDialog0(this).also {
                it.show()
            }
            viewModel.getDebugVersion()
        }

        setting_cityName.setOnClickListener {
            WeatherUtils.openSelectCity(this, object : OnPickListener {
                override fun onPick(position: Int, data: City?) {
                    viewModel.cityName.value = data!!.name
                    WeatherDataKv.setCityName(data.name)

                    val intent = Intent().apply {
                        putExtra("forSetting",data.name)
                    }
                    setResult(21,intent)
                }
                override fun onLocate() {
                    L.d("CityPicker", "onLocate")
                }
                override fun onCancel() {
                    L.d("CityPicker", "onLocate")
                }
            })
        }

    }

    //请求权限
    private fun initPermissionX() {
        PermissionX.init(this)
            .permissions(
                Manifest.permission.ACCESS_FINE_LOCATION,
                )
            .onExplainRequestReason { scope, deniedList -> //拒绝
                val filter = deniedList.filter {
                    it == Manifest.permission.ACCESS_FINE_LOCATION
                }
                scope.showRequestReasonDialog(filter, "授予定位权限使用更加方便哦", "授予", "就不")
            }
            .onForwardToSettings { scope, deniedList -> //拒绝,不再提醒
                val filter = deniedList.filter {
                    it == Manifest.permission.ACCESS_FINE_LOCATION
                }
                scope.showForwardToSettingsDialog(filter, "需要去设置手动开启权限", "明白了", "就不")
            }
            .request { allGranted, grantedList, deniedList ->
                if (allGranted) {
                    val intent = Intent().apply {

                    }
                    setResult(22,intent)
                    permission = true
                    settingGETLocation.visibility = View.GONE
                } else {
                    ToastUtils.show("拒绝了权限")
                }
            }
    }
}