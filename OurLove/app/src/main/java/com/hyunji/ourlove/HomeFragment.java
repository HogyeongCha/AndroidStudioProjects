package com.hyunji.ourlove;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.json.JSONException;
import org.json.JSONObject;

public class HomeFragment extends Fragment {

    private static final String PREF_NAME = "OurLovePrefs";
    private static final String KEY_MY_STATUS = "my_status";
    private static final String KEY_START_DATE = "couple_start_date"; // SetStartDateActivity와 동일하게 사용
    private static final String WEATHER_API_URL = "https://api.open-meteo.com/v1/forecast?latitude=37.5665&longitude=126.9780&current=temperature_2m,weather_code&daily=temperature_2m_max,precipitation_probability_max&timezone=Asia/Seoul&forecast_days=1";

    private TextView tvMyStatus;
    private TextView tvWeatherIcon;
    private TextView tvWeatherTemp;
    private TextView tvWeatherComment;
    private ImageView ivSettings;

    private TextView tvDdayCount;
    private TextView tvAnniversaryInfo;

    private OkHttpClient httpClient;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        httpClient = new OkHttpClient();
    }

    @Override
    public void onResume() {
        super.onResume();
        // 프래그먼트가 다시 활성화될 때 D-DAY를 새로고침 (날짜 변경이 있을 수 있으므로)
        updateDDayInfo();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // D-DAY 관련 TextView 초기화
        tvDdayCount = view.findViewById(R.id.tv_dday_count);
        tvAnniversaryInfo = view.findViewById(R.id.tv_anniversary_info);
        updateDDayInfo(); // D-DAY 정보 업데이트 호출

        // 내 상태 변경 기능 (기존 코드)
        tvMyStatus = view.findViewById(R.id.tv_my_status);
        Button btnStatusEdit = view.findViewById(R.id.btn_my_status_edit);
        loadMyStatus(); // 저장된 상태 불러오기 및 표시
        if (btnStatusEdit != null) {
            btnStatusEdit.setOnClickListener(v -> {
                showStatusEditDialog();
            });
        }

        // 날씨 정보 표시 기능
        tvWeatherIcon = view.findViewById(R.id.tv_weather_icon);
        tvWeatherTemp = view.findViewById(R.id.tv_weather_temp);
        tvWeatherComment = view.findViewById(R.id.tv_weather_comment);
        fetchWeatherData(); // 날씨 데이터 가져오기

        // 설정 아이콘 클릭 리스너 (새로 추가)
        ivSettings = view.findViewById(R.id.iv_settings);
        if (ivSettings != null) {
            ivSettings.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), SettingsActivity.class);
                startActivity(intent);
            });
        }

        return view;
    }

    // D-DAY 정보를 업데이트하는 별도의 메서드
    private void updateDDayInfo() {
        if (getContext() == null || tvDdayCount == null || tvAnniversaryInfo == null) return;

        LocalDate startDate;
        SharedPreferences prefs = getContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String savedStartDateStr = prefs.getString(KEY_START_DATE, null);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            if (savedStartDateStr != null) {
                try {
                    startDate = LocalDate.parse(savedStartDateStr);
                } catch (DateTimeParseException e) {
                    e.printStackTrace();
                    startDate = LocalDate.now(); // 파싱 오류 시 오늘 날짜
                }
            } else {
                startDate = LocalDate.now(); // 저장된 날짜가 없으면 오늘 날짜
            }
        } else {
            startDate = LocalDate.now(); // API 26 미만에서는 LocalDate 사용 불가, 임시
        }

        LocalDate today = LocalDate.now();

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            long dDay = ChronoUnit.DAYS.between(startDate, today) + 1; // 시작일 포함
            tvDdayCount.setText(dDay + "일째");

            long nextAnniversaryDays = ((dDay / 100) + 1) * 100;
            long daysUntilNextAnniversary = nextAnniversaryDays - dDay;
            tvAnniversaryInfo.setText("❤️ " + nextAnniversaryDays + "일까지 D-" + daysUntilNextAnniversary);
        } else {
            tvDdayCount.setText("D-DAY");
            tvAnniversaryInfo.setText("기념일 정보");
        }
    }


    private void loadMyStatus() {
        if (getContext() != null) {
            SharedPreferences prefs = getContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            String savedStatus = prefs.getString(KEY_MY_STATUS, "💻 일하는 중..."); // 기본값
            if (tvMyStatus != null) {
                tvMyStatus.setText("나: " + savedStatus);
            }
        }
    }

    private void saveMyStatus(String status) {
        if (getContext() != null) {
            SharedPreferences prefs = getContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(KEY_MY_STATUS, status);
            editor.apply();
            loadMyStatus(); // 저장 후 상태 업데이트
            Toast.makeText(getContext(), "상태가 저장되었습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private void showStatusEditDialog() {
        if (getContext() == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("내 상태 변경");

        final EditText input = new EditText(getContext());
        if (tvMyStatus != null) {
            String currentStatus = tvMyStatus.getText().toString();
            if (currentStatus.startsWith("나: ")) {
                input.setText(currentStatus.substring(3));
            }
        }
        builder.setView(input);

        builder.setPositiveButton("저장", (dialog, which) -> {
            String newStatus = input.getText().toString();
            if (!newStatus.trim().isEmpty()) {
                saveMyStatus(newStatus);
            } else {
                Toast.makeText(getContext(), "상태를 입력해주세요.", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("취소", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void fetchWeatherData() {
        Request request = new Request.Builder().url(WEATHER_API_URL).build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "날씨 정보를 가져오는 데 실패했습니다.", Toast.LENGTH_SHORT).show());
                }
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String responseData = response.body().string();
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            try {
                                JSONObject jsonObject = new JSONObject(responseData);
                                JSONObject current = jsonObject.getJSONObject("current");
                                JSONObject daily = jsonObject.getJSONObject("daily");

                                double temperature = current.getDouble("temperature_2m");
                                int weatherCode = current.getInt("weather_code");
                                double maxTemperature = daily.getJSONArray("temperature_2m_max").getDouble(0);
                                int precipitationProbabilityMax = daily.getJSONArray("precipitation_probability_max").getInt(0);

                                String temperatureUnit = "°C"; // Open-Meteo 기본 단위는 °C

                                String weatherIcon = getWeatherEmoji(weatherCode, precipitationProbabilityMax);
                                String weatherComment = getWeatherComment(weatherCode, precipitationProbabilityMax);

                                tvWeatherIcon.setText(weatherIcon);
                                tvWeatherTemp.setText(String.format("서울, %.0f%s (최고 %.0f%s)", temperature, temperatureUnit, maxTemperature, temperatureUnit));
                                tvWeatherComment.setText(weatherComment);

                            } catch (JSONException e) {
                                e.printStackTrace();
                                Toast.makeText(getContext(), "날씨 데이터 파싱 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } else {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "날씨 API 응답 실패: " + response.code(), Toast.LENGTH_SHORT).show());
                    }
                }
            }
        });
    }

    // WMO Weather interpretation codes (WW) - Open-Meteo 문서 참조
    // https://www.open-meteo.com/en/docs
    private String getWeatherEmoji(int weatherCode, int precipitationProbability) {
        switch (weatherCode) {
            case 0: // Clear sky
                return "☀️";
            case 1: // Mainly clear
            case 2: // Partly cloudy
                return "🌤️";
            case 3: // Overcast
                return "☁️";
            case 45: // Fog
            case 48: // Depositing rime fog
                return "🌫️";
            case 51: // Drizzle: Light
            case 53: // Drizzle: Moderate
            case 55: // Drizzle: Dense intensity
            case 56: // Freezing Drizzle: Light
            case 57: // Freezing Drizzle: Dense intensity
                return "🌧️";
            case 61: // Rain: Slight
            case 63: // Rain: Moderate
            case 65: // Rain: Heavy intensity
                return "☔";
            case 66: // Freezing Rain: Light
            case 67: // Freezing Rain: Heavy intensity
                return "🧊🌧️";
            case 71: // Snow fall: Slight
            case 73: // Snow fall: Moderate
            case 75: // Snow fall: Heavy intensity
            case 77: // Snow grains
                return "❄️";
            case 80: // Rain showers: Slight
            case 81: // Rain showers: Moderate
            case 82: // Rain showers: Violent
                return "☔";
            case 85: // Snow showers: Slight
            case 86: // Snow showers: Heavy
                return "🌨️";
            case 95: // Thunderstorm: Slight or moderate
            case 96: // Thunderstorm with slight hail
            case 99: // Thunderstorm with heavy hail
                return "⛈️";
            default:
                if (precipitationProbability > 50) {
                    return "☁️🌧️";
                }
                return "❓"; // 알 수 없는 날씨
        }
    }

    private String getWeatherComment(int weatherCode, int precipitationProbability) {
        String baseComment;
        switch (weatherCode) {
            case 0:
            case 1:
                baseComment = "날씨 최고! 오늘 야외 데이트 어때요?";
                break;
            case 2:
            case 3:
                baseComment = "구름이 조금 있지만 괜찮아요. 실내외 데이트 모두 좋아요.";
                break;
            case 45:
            case 48:
                baseComment = "안개가 자욱해요. 안전 운전하고 조심해서 데이트하세요.";
                break;
            case 51:
            case 53:
            case 55:
            case 56:
            case 57:
                baseComment = "이슬비가 내려요. 가벼운 우산 챙기면 문제 없어요.";
                break;
            case 61:
            case 63:
            case 65:
            case 80:
            case 81:
            case 82:
                baseComment = "비가 와요. 실내 데이트나 카페에서 오붓하게 보내는 건 어때요?";
                break;
            case 66:
            case 67:
                baseComment = "빙판길 조심! 오늘은 실내에서 따뜻하게 데이트하세요.";
                break;
            case 71:
            case 73:
            case 75:
            case 77:
            case 85:
            case 86:
                baseComment = "눈이 내려요. 따뜻하게 입고 눈 내리는 풍경을 즐겨보세요.";
                break;
            case 95:
            case 96:
            case 99:
                baseComment = "천둥번개를 동반한 날씨! 안전하게 실내 데이트를 추천해요.";
                break;
            default:
                if (precipitationProbability > 50) {
                    baseComment = "비 또는 눈 소식이 있어요. 실내 데이트를 계획해보세요.";
                }
                else {
                    baseComment = "오늘 날씨는 어떠신가요? 즐거운 하루 보내세요!";
                }
                break;
        }
        return baseComment;
    }
}