package flussonic.watcher.sample.data.network.dto;

import androidx.annotation.Nullable;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;

@AutoValue
public abstract class LoginDto {

    public static TypeAdapter<LoginDto> typeAdapter(Gson gson) {
        return new AutoValue_LoginDto.GsonTypeAdapter(gson);
    }

    @Nullable
    @SerializedName("is_admin")
    public abstract Boolean isAdmin();

    @Nullable
    @SerializedName("notification_email")
    public abstract String notificationEmail();

    @Nullable
    @SerializedName("session")
    public abstract String session();

    @Nullable
    @SerializedName("login")
    public abstract String login();
}
