package cgeo.geocaching.wherigo;

import cgeo.geocaching.R;
import cgeo.geocaching.activity.Keyboard;
import cgeo.geocaching.databinding.WherigoThingDetailsBinding;
import cgeo.geocaching.ui.SimpleItemListModel;
import cgeo.geocaching.ui.TextParam;
import cgeo.geocaching.utils.CommonUtils;
import cgeo.geocaching.utils.EditUtils;
import cgeo.geocaching.utils.LocalizationUtils;
import cgeo.geocaching.utils.offlinetranslate.TranslatorUtils;
import cgeo.geocaching.wherigo.kahlua.vm.LuaTable;
import cgeo.geocaching.wherigo.openwig.Engine;
import cgeo.geocaching.wherigo.openwig.EventTable;
import cgeo.geocaching.wherigo.openwig.Media;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.view.LayoutInflater;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Handles Wherigo/OpenWIG input dialogs */
public class WherigoInputDialogProvider implements IWherigoDialogProvider {

    private final EventTable input;

    /**
     * Handles Wherigo/OpenWIG Input Dialogs. The following is copied from OpenWIG code for reference
     * <p>
     * Request an input from the user.
     * <p>
     * If another dialog or input is open, it should be closed before displaying this input.
     * <p>
     * The <code>input</code> table must contain a "Type" field,
     * which can be either "Text" (then the UI should offer an one-line text input),
     * or "MultipleChoice". In that case, "Choices" field holds
     * another Lua table with list of strings representing the individual choices.
     * UI can then offer either a button for each choice, or some other
     * method of choosing one answer (such as combo box, radio buttons).
     * <p>
     * "Text" field holds a text of this query - this should be displayed above the
     * input field or the choices. "Media" field holds the associated <code>Media</code>.
     * <p>
     * This EventTable has an event "OnGetInput". When the input is processed, this
     * event should be called with a string parameter - either text of the selected choice,
     * or text from the input line. If the input is closed by another API call, the event
     * should be called with null parameter.
     * @param input Lua table describing the input parameters
     */
    WherigoInputDialogProvider(final EventTable input) {
        this.input = input;
    }

    @Override
    public Dialog createAndShowDialog(final Activity activity, final IWherigoDialogControl control) {
        control.setPauseOnDismiss(true);

        final WherigoGame game = WherigoGame.get();

        final AlertDialog dialog = WherigoViewUtils.createFullscreenDialog(activity, LocalizationUtils.getString(R.string.wherigo_player));

        final WherigoThingDetailsBinding binding = WherigoThingDetailsBinding.inflate(LayoutInflater.from(activity));
        dialog.setView(binding.getRoot());

        //translator

        control.disposeOnDismiss(TranslatorUtils.initializeView("InputDialog", activity, control.getTranslator(),
                binding.translation, null, null));

        binding.media.setMedia((Media) input.table.rawget("Media"));
        final Object descr = input.table.rawget("Text");
        control.addTranslation(descr == null ? "" : descr.toString(), (tr, t) -> {
            binding.description.setText(WherigoGame.get().toDisplayText(tr));
        });

        binding.debugBox.setVisibility(game.isDebugModeForCartridge() ? VISIBLE : GONE);
        if (game.isDebugModeForCartridge()) {
            //noinspection SetTextI18n (debug info only)
            binding.debugInfo.setText("Wherigo Input Dialog");
        }

        final String type = (String) input.rawget("InputType");
        boolean handled = false;
        final Map<String, String> choiceTranslations = Collections.synchronizedMap(new HashMap<>());
        final SimpleItemListModel<String> choiceModel = new SimpleItemListModel<>();

        if ("Text".equals(type)) {
            binding.dialogInputLayout.setVisibility(VISIBLE);
            WherigoViewUtils.setViewActions(Arrays.asList(Boolean.FALSE, Boolean.TRUE),
                    binding.dialogActionlist, 2, item -> item ? WherigoUtils.TP_OK_BUTTON : WherigoUtils.TP_CANCEL_BUTTON, item -> {
                if (item) {
                    control.setPauseOnDismiss(false);
                    control.dismiss();
                    Engine.callEvent(input, "OnGetInput", String.valueOf(binding.dialogInputEdittext.getText()));
                } else {
                    control.dismiss();
                }
            });
            EditUtils.setActionListener(binding.dialogInputEdittext, () -> {
                control.setPauseOnDismiss(false);
                control.dismiss();
                Engine.callEvent(input, "OnGetInput", String.valueOf(binding.dialogInputEdittext.getText()));
            });
            Keyboard.show(activity, binding.dialogInputEdittext);

            handled = true;
        }
        if ("MultipleChoice".equals(type)) {
            final LuaTable choicesTable = (LuaTable) input.table.rawget("Choices");
            final List<String> choices = new ArrayList<>(choicesTable.len());
            for (int i = 0; i < choicesTable.len(); i++) {
                final String choiceRaw = (String) choicesTable.rawget((double) (i + 1));
                final String choice = choiceRaw == null ? "-" : choiceRaw;
                choices.add(choice);
                control.addTranslation(choice, (tr, t) -> choiceTranslations.put(choice, tr));
            }

            if (!choices.isEmpty()) {

                binding.dialogItemlistview.setVisibility(VISIBLE);
                choiceModel
                    .setItems(choices)
                    .setDisplayMapper(s -> {
                        final String translated = choiceTranslations.get(s);
                        return TextParam.text(game.toDisplayText(translated != null ? translated : s));
                    })
                    .setSelectedItems(Collections.singleton(choices.get(0)))
                    .setChoiceMode(SimpleItemListModel.ChoiceMode.SINGLE_RADIO);
                binding.dialogItemlistview.setModel(choiceModel);

                WherigoViewUtils.setViewActions(Arrays.asList(Boolean.FALSE, Boolean.TRUE), binding.dialogActionlist, 2,
                    item -> item ? WherigoUtils.TP_OK_BUTTON : WherigoUtils.TP_CANCEL_BUTTON, item -> {
                        if (item) {
                            control.setPauseOnDismiss(false);
                            control.dismiss();
                            Engine.callEvent(input, "OnGetInput", CommonUtils.first(choiceModel.getSelectedItems()));
                        } else {
                            control.dismiss();
                        }
                });
                handled = true;
            }

        }

        if (!handled) {
            WherigoViewUtils.setViewActions(Collections.singleton("ok"), binding.dialogActionlist, 1, item -> WherigoUtils.TP_OK_BUTTON, item -> {
                control.setPauseOnDismiss(false);
                control.dismiss();
                Engine.callEvent(input, "OnGetInput", null);
            });
        }

        //retrigger choice paint on trasnlation events
        control.setOnGameNotificationListener((d, nt) -> choiceModel.triggerRepaint());

        dialog.show();
        return dialog;
    }
}
