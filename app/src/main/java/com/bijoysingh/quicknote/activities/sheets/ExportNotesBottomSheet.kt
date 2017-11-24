package com.bijoysingh.quicknote.activities.sheets

import android.app.Dialog
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import com.bijoysingh.quicknote.R
import com.bijoysingh.quicknote.activities.MainActivity
import com.bijoysingh.quicknote.activities.external.getNotesForExport
import com.bijoysingh.quicknote.activities.external.saveFile
import com.github.bijoysingh.starter.async.MultiAsyncTask
import com.github.bijoysingh.starter.fragments.SimpleBottomSheetFragment
import android.content.Intent
import android.net.Uri
import com.bijoysingh.quicknote.activities.external.getExportFile
import com.bijoysingh.quicknote.utils.GenericFileProvider
import java.io.File


class ExportNotesBottomSheet : SimpleBottomSheetFragment() {

  override fun setupView(dialog: Dialog?) {
    if (dialog == null) {
      return
    }

    val exportTitle = dialog.findViewById<TextView>(R.id.export_title)
    val progressBar = dialog.findViewById<ProgressBar>(R.id.progress_bar)
    val resultLayout = dialog.findViewById<View>(R.id.results_layout)
    val exportDone = dialog.findViewById<View>(R.id.export_done)
    val exportShare = dialog.findViewById<View>(R.id.export_share)

    MultiAsyncTask.execute(activity, object : MultiAsyncTask.Task<Boolean> {
      override fun run(): Boolean {
        val notes = getNotesForExport(activity)
        return saveFile(activity, notes)
      }

      override fun handle(result: Boolean) {
        resultLayout.visibility = View.VISIBLE
        progressBar.visibility = View.INVISIBLE
        exportTitle.setText(
            if (result) R.string.import_export_layout_exported
            else R.string.import_export_layout_export_failed)
        exportDone.visibility = if (result) View.VISIBLE else View.GONE
      }
    })
    exportDone.setOnClickListener {
      dismiss()
    }
    exportShare.setOnClickListener {
      val file = getExportFile()
      if (file == null || !file.exists()) {
        return@setOnClickListener
      }

      val uri = GenericFileProvider.getUriForFile(activity, GenericFileProvider.PROVIDER, file)

      val intent = Intent(Intent.ACTION_SEND)
      intent.type = "text/plain"
      intent.putExtra(Intent.EXTRA_STREAM, uri)
      startActivity(Intent.createChooser(intent, getString(R.string.share_using)))

      dismiss()g
    }
  }

  override fun getLayout(): Int = R.layout.bottom_sheet_import_export

  companion object {

    val MATERIAL_NOTES_FOLDER = "MaterialNotes"
    val FILE_NAME = "BACKUP"

    fun openSheet(activity: MainActivity) {
      val sheet = ExportNotesBottomSheet()
      sheet.show(activity.supportFragmentManager, sheet.tag)
    }


  }
}