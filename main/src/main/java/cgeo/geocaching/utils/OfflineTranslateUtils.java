package cgeo.geocaching.utils;

import cgeo.geocaching.R;
import cgeo.geocaching.activity.AbstractActivity;
import cgeo.geocaching.activity.TabbedViewPagerActivity;
import cgeo.geocaching.settings.Settings;
import cgeo.geocaching.ui.SimpleItemListModel;
import cgeo.geocaching.ui.TextParam;
import cgeo.geocaching.ui.dialog.SimpleDialog;
import cgeo.geocaching.utils.offlinetranslate.ITranslatorImpl;
import cgeo.geocaching.utils.offlinetranslate.TranslateAccessor;
import cgeo.geocaching.utils.offlinetranslate.TranslationModelManager;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.google.android.material.button.MaterialButton;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.TextNode;

public class OfflineTranslateUtils {

    private OfflineTranslateUtils() {
        // utility class
    }

    public static final String LANGUAGE_UNKNOWN = "und";
    public static final String LANGUAGE_UNDELETABLE = "en";
    public static final String LANGUAGE_INVALID = "";
    public static final String LANGUAGE_AUTOMATIC = "default";

    public static List<Language> getSupportedLanguages() {
        final List<Language> languages = new ArrayList<>();
        final Set<String> languageIds = TranslateAccessor.get().getSupportedLanguages();
        for (String languageId : languageIds) {
            languages.add(new Language(languageId));
        }
        return languages.stream().sorted().collect(Collectors.toList());
    }

    /**
     * Utility method to override the source language, shows a list of all available languages
     * @param context   calling context
     * @param consumer  selected language
     */
    public static void showLanguageSelection(final Context context, final Consumer<Language> consumer) {
        final SimpleDialog.ItemSelectModel<Language> model = new SimpleDialog.ItemSelectModel<>();
        model.setItems(getSupportedLanguages())
                .setDisplayMapper((l) -> TextParam.text(l.toString()))
                .setChoiceMode(SimpleItemListModel.ChoiceMode.SINGLE_PLAIN);

        SimpleDialog.ofContext(context).setTitle(R.string.translator_language_select)
                .selectSingle(model, consumer::accept);
    }

    /**
     * Checks if the language model for the source (from parameter) or target (from setting) language are missing
     */
    private static void findMissingLanguageModels(final String lngCode, final Consumer<List<Language>> consumer) {
        getDownloadedLanguageModels(availableLanguages -> {
            final List<Language> missingLanguages = new ArrayList<>();
            if (!availableLanguages.contains(lngCode)) {
                missingLanguages.add(new Language(lngCode));
            }
            final Language targetLanguage = Settings.getTranslationTargetLanguage();
            if (!availableLanguages.contains(targetLanguage.getCode())) {
                missingLanguages.add(targetLanguage);
            }
            consumer.accept(missingLanguages);
        });
    }

    public static void detectLanguage(final String text, final Consumer<Language> successConsumer, final Consumer<String> errorConsumer) {
        // identify listing language
        TranslateAccessor.get().guessLanguage(text,
            lngCode -> successConsumer.accept(new Language(lngCode)),
                e -> errorConsumer.accept(e.getMessage()));
    }

    public static void translateTextAutoDetectLng(final Activity activity, final Status translationStatus, final String text, final Consumer<Language> unsupportedLngConsumer, final Consumer<List<Language>> downloadingModelConsumer, final Consumer<ITranslatorImpl> translatorConsumer) {
        detectLanguage(text, lng -> {
            getTranslator(activity, translationStatus, lng, unsupportedLngConsumer, downloadingModelConsumer, translatorConsumer);
        }, e -> {

        });
    }

    /**
     * Utility method to get a Translator object that can be used to perform offline translation
     * @param activity      the calling activity
     * @param sourceLng     language to translate from
     * @param unsupportedLngConsumer    returned if the sourceLng is not supported
     * @param downloadingModelConsumer  returned if language models need to be downloaded, should be used to show a progress UI to the user
     * @param translatorConsumer        returns the translator object
     */
    public static void getTranslator(final Activity activity, final Status translationStatus, final Language sourceLng, final Consumer<Language> unsupportedLngConsumer, final Consumer<List<OfflineTranslateUtils.Language>> downloadingModelConsumer, final Consumer<ITranslatorImpl> translatorConsumer) {
        if (!isLanguageSupported(sourceLng)) {
            unsupportedLngConsumer.accept(sourceLng);
            return;
        }

        final String sourceLngCode = TranslateAccessor.get().fromLanguageTag(sourceLng.getCode());
        findMissingLanguageModels(sourceLngCode, missingLanguageModels -> {
            if (missingLanguageModels.isEmpty()) {
                OfflineTranslateUtils.getTranslator(sourceLngCode, translatorConsumer);
            } else {
                SimpleDialog.of(activity).setTitle(R.string.translator_model_download_confirm_title).setMessage(TextParam.id(R.string.translator_model_download_confirm_txt, String.join(", ", missingLanguageModels.stream().map(OfflineTranslateUtils.Language::toString).collect(Collectors.toList())))).confirm(() -> {
                    downloadingModelConsumer.accept(missingLanguageModels);
                    OfflineTranslateUtils.getTranslator(sourceLngCode, translatorConsumer);
                },
                    () -> translationStatus.abortTranslation()
                );
            }
        });
    }

    public static void translateParagraph(final ITranslatorImpl translator, final OfflineTranslateUtils.Status status, final String text, final Consumer<String> consumer, final Consumer<Exception> errorConsumer) {
        final Document document = Jsoup.parseBodyFragment(text);
        final List<TextNode> elements = document.children().select("*").textNodes();
        final AtomicInteger remaining = new AtomicInteger(elements.size());
        if (remaining.get() == 0) {
            consumer.accept("");
            status.updateProgress();
        } else {
            for (TextNode textNode : elements) {
                translator.translate(textNode.text(),
                        translation -> {
                            textNode.text(translation);
                            // check if all done
                            if (remaining.decrementAndGet() == 0) {
                                consumer.accept(document.body().html());
                                status.updateProgress();
                            }
                        }, e -> {
                            Log.e("err: " + e.getMessage());
                            status.abortTranslation();
                            errorConsumer.accept(e);
                        });
            }
        }
    }

    private static void getTranslator(final String lng, final Consumer<ITranslatorImpl> consumer) {
        final String targetLng = TranslateAccessor.get().fromLanguageTag(Settings.getTranslationTargetLanguage().getCode());
        if (null == targetLng) {
            return;
        }

        TranslateAccessor.get().getTranslatorWithDownload(lng, targetLng, consumer, e -> {
            Log.e("Failed to initialize MLKit Translator", e);
            consumer.accept(null);
        });
    }

    private static void getDownloadedLanguageModels(final Consumer<List<String>> consumer) {
        final List<String> result = new ArrayList<>();
        for (String lng : TranslationModelManager.get().getSupportedLanguages()) {
            if (TranslationModelManager.get().isAvailable(lng)) {
                result.add(lng);
            }
        }
        consumer.accept(result);
    }

    public static void deleteLanguageModel(final String lngCode) {
        TranslationModelManager.get().deleteLanguage(lngCode);
    }

    public static void downloadLanguageModels(final Context context) {
        final List<Language> languages = getSupportedLanguages();
        getDownloadedLanguageModels(availableLanguages -> {
            languages.removeAll(availableLanguages.stream().map(Language::new).collect(Collectors.toList()));
            Collections.sort(languages);

            final SimpleDialog.ItemSelectModel<Language> model = new SimpleDialog.ItemSelectModel<>();
            model.setItems(languages)
                    .setDisplayMapper((l) -> TextParam.text(l.toString()))
                    .setChoiceMode(SimpleItemListModel.ChoiceMode.MULTI_CHECKBOX);

            SimpleDialog.ofContext(context).setTitle(R.string.translator_model_download_select)
                    .selectMultiple(model, lngs -> {
                        for (Language lng : lngs) {
                            TranslationModelManager.get().downloadLanguage(lng.getCode());
                        }
                    });
        });
    }

    public static void initializeListingTranslatorInTabbedViewPagerActivity(final TabbedViewPagerActivity cda, final LinearLayout translationBox, final String translateText, final Runnable callback) {
        if (!OfflineTranslateUtils.isTargetLanguageValid() || TranslateAccessor.get().getSupportedLanguages().isEmpty() ||
                cda == null || cda.isFinishing() || cda.isDestroyed()) {
            AndroidRxUtils.runOnUi(() -> translationBox.setVisibility(View.GONE));
            return;
        }
        final TextView note = translationBox.findViewById(R.id.description_translate_note);
        final Button button = translationBox.findViewById(R.id.description_translate_button);

        // add observer to listing language
        cda.translationStatus.setSourceLanguageChangeConsumer(lng -> {
            final boolean showTranslationBox;
            if (null == lng || Settings.getLanguagesToNotTranslate().contains(lng.getCode())) {
                showTranslationBox = false;
            } else {
                showTranslationBox = true;
                if (OfflineTranslateUtils.LANGUAGE_UNKNOWN.equals(lng.getCode())) {
                    button.setEnabled(false);
                    note.setText(R.string.translator_language_unknown);
                } else {
                    button.setEnabled(true);
                    note.setText(cda.getResources().getString(R.string.translator_language_detected, lng.toString()));
                }
            }
            AndroidRxUtils.runOnUi(() -> translationBox.setVisibility(showTranslationBox ? View.VISIBLE : View.GONE));
        });

        button.setOnClickListener(v -> callback.run());
        note.setOnClickListener(v -> OfflineTranslateUtils.showLanguageSelection(cda, cda.translationStatus::setSourceLanguage));

        // identify listing language
        OfflineTranslateUtils.detectLanguage(translateText,
                cda.translationStatus::setSourceLanguage,
                error -> {
                    AndroidRxUtils.runOnUi(() -> translationBox.setVisibility(View.VISIBLE));
                    button.setEnabled(false);
                    note.setText(error);
                });
    }

    public static OfflineTranslateUtils.Language getAppLanguageOrDefault() {
        final OfflineTranslateUtils.Language appLanguage = Settings.getApplicationLanguage();
        if (OfflineTranslateUtils.isLanguageSupported(appLanguage)) {
            return appLanguage;
        } else {
            return new OfflineTranslateUtils.Language(OfflineTranslateUtils.LANGUAGE_UNDELETABLE);
        }
    }

    public static boolean isTargetLanguageValid() {
        return Settings.getTranslationTargetLanguage().isValid();
    }

    public static boolean isLanguageSupported(final Language language) {
        final String languageTag = null != language ? language.getCode() : null;
        if (null == languageTag) {
            return false;
        }
        final String languageCode = TranslateAccessor.get().fromLanguageTag(languageTag);
        return (null != languageCode);
    }

    public static class TranslationProgressHandler extends ProgressButtonDisposableHandler {
        public TranslationProgressHandler(final AbstractActivity activity) {
            super(activity);
        }
    }

    public static class Language implements Comparable<Language> {
        private final String code;

        public Language(final String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }

        public boolean isValid() {
            return code != null && !LANGUAGE_INVALID.equals(code);
        }

        public String getDisplayName() {
            return code == null ? "--" : LocalizationUtils.getLocaleDisplayName(new Locale(code), true, true);
        }

        @NonNull
        @Override
        public String toString() {
            return getDisplayName();
        }

        @Override
        public boolean equals(final Object o) {
            if (!(o instanceof Language)) {
                return false;
            }
            return ((Language) o).getCode().equals(getCode());
        }

        @Override
        public int hashCode() {
            return code.hashCode();
        }

        @Override
        public int compareTo(@NonNull final Language lng) {
            return this.getCode().compareTo(lng.getCode());
        }
    }

    public static class Status {
        private Language sourceLanguage = new Language(LANGUAGE_INVALID);
        private TranslationProgressHandler progressHandler;
        private boolean isTranslated = false;
        private int textsToTranslate;
        private int translatedTexts = 0;
        private Consumer<Language> languageChangeConsumer;
        private boolean needsRetranslation = false;

        public Language getSourceLanguage() {
            return sourceLanguage;
        }

        public synchronized void setSourceLanguage(final OfflineTranslateUtils.Language lng) {
            sourceLanguage = lng;
            if (null != this.languageChangeConsumer) {
                this.languageChangeConsumer.accept(lng);
            }
        }

        public void setSourceLanguageChangeConsumer(final Consumer<Language> consumer) {
            this.languageChangeConsumer = consumer;
        }

        public boolean isInProgress() {
            return progressHandler != null && !progressHandler.isDisposed();
        }

        public void setProgressHandler(final OfflineTranslateUtils.TranslationProgressHandler progressHandler) {
            this.progressHandler = progressHandler;
        }

        public synchronized void startTranslation(final int textsToTranslate, @Nullable final AbstractActivity activity, @Nullable final MaterialButton button) {
            setNotTranslated();
            this.textsToTranslate = textsToTranslate;
            this.translatedTexts = 0;
            if (null != activity) {
                this.progressHandler = new OfflineTranslateUtils.TranslationProgressHandler(activity);
                this.progressHandler.showProgress(button);
            }
        }

        public synchronized void abortTranslation() {
            if (null != this.progressHandler) {
                this.progressHandler.sendEmptyMessage(DisposableHandler.DONE);
            }
            setNotTranslated();
        }

        public synchronized void updateProgress() {
            if (this.textsToTranslate == ++this.translatedTexts) {
                isTranslated = true;
                if (null != this.progressHandler) {
                    this.progressHandler.sendEmptyMessage(DisposableHandler.DONE);
                }
            }
        }

        public boolean isTranslated() {
            return isTranslated;
        }

        public void setNotTranslated() {
            this.isTranslated = false;
            this.needsRetranslation = false;
        }


        public void setNeedsRetranslation() {
            needsRetranslation = true;
        }

        public boolean checkRetranslation() {
            return needsRetranslation && !isInProgress()
                    && getSourceLanguage().isValid();
        }
    }
}
