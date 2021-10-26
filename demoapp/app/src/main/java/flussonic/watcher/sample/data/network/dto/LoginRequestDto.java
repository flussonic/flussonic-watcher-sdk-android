package flussonic.watcher.sample.data.network.dto;

import androidx.annotation.NonNull;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;

@AutoValue
public abstract class LoginRequestDto {

    public static TypeAdapter<LoginRequestDto> typeAdapter(Gson gson) {
        return new AutoValue_LoginRequestDto.GsonTypeAdapter(gson);
    }

    public static LoginRequestDto create(@NonNull String login, @NonNull String password) {
        return new AutoValue_LoginRequestDto(login, password);
    }

    @SerializedName("login")
    public abstract String login();

    @SerializedName("password")
    public abstract String password();
}
