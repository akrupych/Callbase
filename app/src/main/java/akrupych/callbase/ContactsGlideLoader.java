package akrupych.callbase;

import android.content.Context;
import android.provider.ContactsContract;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.data.DataFetcher;
import com.bumptech.glide.load.model.stream.StreamModelLoader;

import java.io.InputStream;

public class ContactsGlideLoader implements StreamModelLoader<String> {

    private final Context context;

    public ContactsGlideLoader(Context context) {
        this.context = context;
    }

    @Override
    public DataFetcher<InputStream> getResourceFetcher(final String number, int width, int height) {
        return new DataFetcher<InputStream>() {
            @Override
            public InputStream loadData(Priority priority) throws Exception {
                return ContactsContract.Contacts.openContactPhotoInputStream(
                        context.getContentResolver(), ContactFetcher.getContactUri(context, number));
            }

            @Override
            public void cleanup() {

            }

            @Override
            public String getId() {
                return number;
            }

            @Override
            public void cancel() {

            }
        };
    }
}
