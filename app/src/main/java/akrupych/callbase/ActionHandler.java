package akrupych.callbase;

public interface ActionHandler {

    void itemClick(int position);

    void call(String number);

    void sms(String number);

    void openContact(String number);

    void copy(String number);
}
