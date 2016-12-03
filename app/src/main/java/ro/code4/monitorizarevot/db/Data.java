package ro.code4.monitorizarevot.db;

import android.support.annotation.NonNull;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import ro.code4.monitorizarevot.net.model.Branch;
import ro.code4.monitorizarevot.net.model.Form;
import ro.code4.monitorizarevot.net.model.Note;
import ro.code4.monitorizarevot.net.model.Question;
import ro.code4.monitorizarevot.net.model.Section;
import ro.code4.monitorizarevot.net.model.Version;

public class Data {
    private static final String AUTO_INCREMENT_PRIMARY_KEY = "id";
    private static Data instance;
    private Realm realm;

    public static Data getInstance() {
        if (instance == null) {
            instance = new Data();
        }
        return instance;
    }

    private static int getNextPrimaryKey(Realm realm, Class realmClass) {
        Number maxPrimaryKeyValue = realm.where(realmClass).max(AUTO_INCREMENT_PRIMARY_KEY);
        return maxPrimaryKeyValue != null ? maxPrimaryKeyValue.intValue() + 1 : 0;
    }

    private Data() {

    }

    public Form getFormA() {
        return getForm("A");
    }

    public Form getFormB() {
        return getForm("B");
    }

    public Form getFormC() {
        return getForm("C");
    }

    public Form getForm(String formId) {
        realm = Realm.getDefaultInstance();
        RealmResults<Form> results = realm
                .where(Form.class)
                .equalTo("id", formId)
                .findAll();
        Form result = results.size() > 0 ? realm.copyFromRealm(results.get(0)) : null;
        realm.close();
        return result;
    }

    public Version getFormVersion() {
        RealmResults<Version> queryResult = Realm.getDefaultInstance()
                .where(Version.class)
                .findAll();
        return queryResult.size() > 0 ? queryResult.first() : null;
    }

    public List<Note> getNotes() {
        realm = Realm.getDefaultInstance();
        RealmResults<Note> result = realm
                .where(Note.class)
                .findAll();
        List<Note> notes = realm.copyFromRealm(result);
        realm.close();
        return notes;
    }

    public List<Question> getUnSyncedQuestions(){
        realm = Realm.getDefaultInstance();
        RealmResults<Question> questions = realm.where(Question.class)
                .equalTo("isSynced", false)
                .findAll();
        List<Question> unSyncedQuestions = realm.copyFromRealm(questions);
        realm.close();
        return unSyncedQuestions;
    }

    public Question getQuestion(Integer questionId) {
        realm = Realm.getDefaultInstance();
        Question result = realm
                .where(Question.class)
                .equalTo("idIntrebare", questionId)
                .findFirst();
        Question question = realm.copyFromRealm(result);
        realm.close();
        return question;
    }

    public void saveAnswerResponse(Branch branch) {
        realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(branch);
        realm.commitTransaction();
        realm.close();
    }

    public void saveFormDefinition(String formId, List<Section> sections) {
        realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        Form form = realm.createObject(Form.class, formId);
        form.setSections(new RealmList<Section>());
        form.getSections().addAll(sections);
        realm.commitTransaction();
        realm.close();
    }

    public void saveFormsVersion(Version version) {
        realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(version);
        realm.commitTransaction();
        realm.close();
    }

    public void saveNote(String uriPath, String description, Integer questionId) {
        realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        Note note = realm.createObject(Note.class, getNextPrimaryKey(realm, Note.class));
        note.setUriPath(uriPath);
        note.setDescription(description);
        note.setQuestionId(questionId);
        realm.commitTransaction();
        realm.close();
    }

    public void updateQuestionStatus(Integer questionId) {
        realm = Realm.getDefaultInstance();
        Question question = realm
                .where(Question.class)
                .equalTo("idIntrebare", questionId)
                .findFirst();

        realm.beginTransaction();
        question.setSynced(true);
        realm.copyToRealmOrUpdate(question);
        realm.commitTransaction();

        realm.close();
    }

    public void deleteNote(Note note) {
        realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        RealmResults<Note> results = realm.where(Note.class)
                .equalTo("id", note.getId())
                .findAll();
        results.deleteAllFromRealm();
        realm.commitTransaction();
        realm.close();
    }

    public Branch getCityBranch(Integer quetionId) {
        realm = Realm.getDefaultInstance();
        Branch result = realm
                .where(Branch.class)
                .equalTo("cityBranchId", getCityBranchId(quetionId))
                .findFirst();
        Branch branch = result != null ? realm.copyFromRealm(result) : null;
        realm.close();
        return branch;
    }

    @NonNull
    private String getCityBranchId(Integer quetionId) {
        return Preferences.getCountyCode() +
                String.valueOf(Preferences.getBranchNumber()) +
                String.valueOf(quetionId);
    }

    public List<Branch> getCityBranchPerQuestion(Integer quetionId) {
        realm = Realm.getDefaultInstance();
        RealmResults<Branch> result = realm
                .where(Branch.class)
                .equalTo("cityBranchId", getCityBranchId(quetionId))
                .findAll();
        List<Branch> branches = realm.copyFromRealm(result);
        realm.close();
        return branches;
    }
}