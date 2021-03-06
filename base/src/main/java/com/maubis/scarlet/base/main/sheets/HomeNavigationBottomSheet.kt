package com.maubis.scarlet.base.main.sheets

import android.app.Dialog
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.github.bijoysingh.starter.async.MultiAsyncTask
import com.github.bijoysingh.starter.util.LocaleManager
import com.github.bijoysingh.uibasics.views.UITextView
import com.maubis.scarlet.base.MainActivity
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.config.CoreConfig
import com.maubis.scarlet.base.core.tag.TagBuilder
import com.maubis.scarlet.base.main.HomeNavigationState
import com.maubis.scarlet.base.note.tag.TagOptionsItem
import com.maubis.scarlet.base.note.tag.sheet.CreateOrEditTagBottomSheet
import com.maubis.scarlet.base.note.tag.view.HomeTagView
import com.maubis.scarlet.base.settings.sheet.SettingsOptionsBottomSheet
import com.maubis.scarlet.base.support.database.notesDB
import com.maubis.scarlet.base.support.database.tagsDB
import com.maubis.scarlet.base.support.option.OptionsItem
import com.maubis.scarlet.base.support.sheets.GridBottomSheetBase
import com.maubis.scarlet.base.support.ui.ThemeColorType

class HomeNavigationBottomSheet : GridBottomSheetBase() {

  override fun setupViewWithDialog(dialog: Dialog) {
    resetOptions(dialog)
    resetTags(dialog)
    setAddTagOption(dialog)
  }

  private fun getOptions(): List<OptionsItem> {
    val activity = context as MainActivity
    val options = ArrayList<OptionsItem>()
    options.add(OptionsItem(
        title = R.string.nav_home,
        subtitle = R.string.nav_home_details,
        icon = R.drawable.ic_home_white_48dp,
        selected = activity.mode == HomeNavigationState.DEFAULT,
        listener = View.OnClickListener {
          activity.onHomeClick();
          dismiss();
        }
    ))
    options.add(OptionsItem(
        title = R.string.nav_favourites,
        subtitle = R.string.nav_favourites_details,
        icon = R.drawable.ic_favorite_white_48dp,
        selected = activity.mode == HomeNavigationState.FAVOURITE,
        listener = View.OnClickListener {
          activity.onFavouritesClick();
          dismiss();
        }
    ))
    options.add(OptionsItem(
        title = R.string.nav_archived,
        subtitle = R.string.nav_archived_details,
        icon = R.drawable.ic_archive_white_48dp,
        selected = activity.mode == HomeNavigationState.ARCHIVED,
        listener = View.OnClickListener {
          activity.onArchivedClick();
          dismiss();
        }
    ))
    options.add(OptionsItem(
        title = R.string.nav_locked,
        subtitle = R.string.nav_locked_details,
        icon = R.drawable.ic_action_lock,
        selected = activity.mode == HomeNavigationState.LOCKED,
        listener = View.OnClickListener {
          activity.onLockedClick();
          dismiss();
        }
    ))
    options.add(OptionsItem(
        title = R.string.nav_trash,
        subtitle = R.string.nav_trash_details,
        icon = R.drawable.ic_delete_white_48dp,
        selected = activity.mode == HomeNavigationState.TRASH,
        listener = View.OnClickListener {
          activity.onTrashClick();
          dismiss();
        }
    ))
    options.add(OptionsItem(
        title = R.string.nav_settings,
        subtitle = R.string.nav_settings,
        icon = R.drawable.ic_action_settings,
        listener = View.OnClickListener {
          SettingsOptionsBottomSheet.openSheet(activity)
          dismiss();
        }
    ))
    return options
  }

  fun resetOptions(dialog: Dialog) {
    MultiAsyncTask.execute(themedActivity(), object : MultiAsyncTask.Task<List<OptionsItem>> {
      override fun run(): List<OptionsItem> = getOptions()
      override fun handle(result: List<OptionsItem>) {
        val titleView = dialog.findViewById<TextView>(R.id.options_title)
        titleView.setTextColor(CoreConfig.instance.themeController().get(ThemeColorType.SECONDARY_TEXT))

        val separator = dialog.findViewById<View>(R.id.separator)
        separator.setBackgroundColor(CoreConfig.instance.themeController().get(ThemeColorType.HINT_TEXT))

        setOptions(dialog, result)
      }
    })
  }

  fun resetTags(dialog: Dialog) {
    MultiAsyncTask.execute(themedActivity(), object : MultiAsyncTask.Task<List<TagOptionsItem>> {
      override fun run(): List<TagOptionsItem> = getTagOptions()
      override fun handle(result: List<TagOptionsItem>) {
        val titleView = dialog.findViewById<TextView>(R.id.tag_options_title)
        titleView.setTextColor(CoreConfig.instance.themeController().get(ThemeColorType.SECONDARY_TEXT))

        val layout = dialog.findViewById<LinearLayout>(R.id.options_container)
        layout.removeAllViews()
        setTagOptions(dialog, result)
      }
    })
  }

  fun setTagOptions(dialog: Dialog, options: List<TagOptionsItem>) {
    val layout = dialog.findViewById<LinearLayout>(R.id.options_container);
    for (option in options.sorted()) {
      val contentView = HomeTagView(View.inflate(context, R.layout.layout_home_tag_item, null))
      contentView.title.setText(option.tag.title)
      contentView.rootView.setOnClickListener(option.listener)
      contentView.subtitle.visibility = View.GONE
      contentView.icon.setImageResource(option.getIcon())

      contentView.action.setImageResource(option.getEditIcon());
      contentView.action.setColorFilter(CoreConfig.instance.themeController().get(ThemeColorType.HINT_TEXT));
      contentView.action.setOnClickListener(option.editListener)

      if (option.usages > 0) {
        contentView.subtitle.setText(LocaleManager.toString(option.usages))
        contentView.subtitle.visibility = View.VISIBLE
      }

      contentView.title.setTextColor(getOptionsTitleColor(option.selected))
      contentView.subtitle.setTextColor(getOptionsSubtitleColor(option.selected))
      contentView.icon.setColorFilter(getOptionsTitleColor(option.selected))

      layout.addView(contentView.rootView)
    }
  }

  fun getTagOptions(): List<TagOptionsItem> {
    val activity = context as MainActivity
    val options = ArrayList<TagOptionsItem>()
    for (tag in tagsDB.getAll()) {
      options.add(TagOptionsItem(
          tag = tag,
          usages = notesDB.getNoteCountByTag(tag.uuid),
          listener = View.OnClickListener {
            activity.openTag(tag)
            dismiss()
          },
          editable = true,
          editListener = View.OnClickListener {
            CreateOrEditTagBottomSheet.openSheet(activity, tag, { _, _ -> resetTags(dialog) })
          }
      ))
    }
    return options
  }

  fun setAddTagOption(dialog: Dialog) {
    val newTagButton = dialog.findViewById<UITextView>(R.id.new_tag_button);
    newTagButton.setTextColor(CoreConfig.instance.themeController().get(ThemeColorType.HINT_TEXT))
    newTagButton.setImageTint(CoreConfig.instance.themeController().get(ThemeColorType.HINT_TEXT))
    newTagButton.setOnClickListener { onNewTagClick() }
    newTagButton.icon.alpha = 0.6f
  }

  fun onNewTagClick() {
    val activity = context as MainActivity
    CreateOrEditTagBottomSheet.openSheet(activity, TagBuilder().emptyTag(), { _, _ -> resetTags(dialog) })
  }

  override fun getLayout(): Int = R.layout.bottom_sheet_home_navigation

  companion object {
    fun openSheet(activity: MainActivity) {
      val sheet = HomeNavigationBottomSheet()

      sheet.show(activity.supportFragmentManager, sheet.tag)
    }
  }
}