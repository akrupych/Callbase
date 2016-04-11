package akrupych.callbase;

public interface ActionHandler {

    void call(String number);

    void sms(String number);

    void openContact(String number);

}
