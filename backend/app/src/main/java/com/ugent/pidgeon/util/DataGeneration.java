package com.ugent.pidgeon.util;

import com.ugent.pidgeon.controllers.CourseController;
import com.ugent.pidgeon.controllers.ProjectController;
import com.ugent.pidgeon.model.Auth;
import com.ugent.pidgeon.model.json.*;
import com.ugent.pidgeon.postgre.models.CourseEntity;
import com.ugent.pidgeon.postgre.models.UserEntity;
import com.ugent.pidgeon.postgre.models.types.CourseRelation;
import com.ugent.pidgeon.postgre.models.types.UserRole;
import com.ugent.pidgeon.postgre.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.logging.Logger;

@Component
public class DataGeneration {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseController courseController;

    @Autowired
    private ProjectController projectController;


    public void generate(Auth auth) {
        makeFakeUsersAndCourses(auth);
    }

    private void makeFakeUsersAndCourses(Auth auth) {
        String[] vaktitels = {"Computationele Biologie", "Computer Architectuur", "Mobile and Broadband Access Networks",
        "Wiskundige modelering", "Design of Multimedia Applications"};
        for (int i = 0; i < 5; i++) {
            UserEntity user = new UserEntity(
                    "teacher",
                    "number ".concat(String.valueOf(i)),
                    "teacher.number".concat(String.valueOf(i)).concat("@ugent.be"),
                    UserRole.teacher,
                    "azure_id_number_teacher_".concat(String.valueOf(i)),
                    "teacher".concat(String.valueOf(i * 1000))
            );
            userRepository.save(user);
            CourseJson cj = new CourseJson(
                    vaktitels[i],
                    "# VAK\n Dit vak gaat over vanalles.",
                    false,
                    2023
            );
            ResponseEntity<?> resp = courseController.createCourse(cj, auth);
            if (resp.getStatusCode().is2xxSuccessful()) {
                CourseMemberRequestJson cmrj = new CourseMemberRequestJson();
                if (i == 0 || i >= 3) {
                    cmrj.setRelation(CourseRelation.creator.toString());
                } else {
                    cmrj.setRelation(CourseRelation.course_admin.toString());
                }
                cmrj.setUserId(user.getId());
                CourseWithInfoJson course = (CourseWithInfoJson) resp.getBody();
                if (course != null) {
                    courseController.addCourseMember(auth, course.courseId(), cmrj);
                    RelationRequest rr = new RelationRequest();
                    if (i >= 3) {
                        rr.setRelation(CourseRelation.enrolled.toString());
                    } else if (i > 0) {
                        rr.setRelation(CourseRelation.creator.toString());
                    } else {
                        rr.setRelation(CourseRelation.course_admin.toString());
                    }
                    courseController.updateCourseMember(auth, course.courseId(), rr, auth.getUserEntity().getId());
                    cmrj.setRelation(CourseRelation.enrolled.toString());
                    for (int j = 0; j < 10; j++) {
                        UserEntity u = getStudentUserEntity(i, j);
                        userRepository.save(u);
                        cmrj.setUserId(u.getId());
                        courseController.addCourseMember(auth, course.courseId(), cmrj);
                    }
                    ProjectResponseJsonWithStatus project =  makeProject(auth, course, i % 3);
                    
                }
            }
        }
    }

    private void makeSubmissions(Auth auth, ) {

    }

    private ProjectResponseJsonWithStatus makeProject(Auth auth, CourseWithInfoJson course, int index) {
        String[] projectTitles = {"A fantastic project", "A project about python dictionaries", "A palindrome checker"};
        String[] projectDescriptions = {"Stel je voor: een project dat de wereld zal veranderen. Een project dat verder gaat dan de grenzen van wat we ons ooit konden voorstellen. Dit is niet zomaar een project; het is een revolutie in denken, een transformatie in de manier waarop we leven, werken en dromen. Het heet \"Elysium Innovatus,\" en het belooft de wereld op zijn kop te zetten.\n" +
                "\n" +
                "\"Elysium Innovatus\" is een buitengewoon project dat is ontworpen om de kloof tussen technologie en menselijkheid te overbruggen. Stel je een toekomst voor waarin kunstmatige intelligentie niet alleen een hulpmiddel is, maar een partner, een metgezel die ons helpt bij het navigeren door de complexiteiten van het moderne leven. Dit project is de belichaming van deze visie. Door gebruik te maken van de nieuwste doorbraken in machine learning en neurowetenschappen, streeft \"Elysium Innovatus\" ernaar om een symbiotische relatie te creëren tussen mens en machine, waarbij de sterke punten van beide worden benut om een betere, meer harmonieuze wereld te bouwen.\n" +
                "\n" +
                "Een van de pijlers van dit project is de ontwikkeling van hypergeavanceerde AI-assistenten die niet alleen onze dagelijkse taken kunnen automatiseren, maar ook in staat zijn om emotioneel intelligent te reageren. Deze assistenten kunnen ons helpen bij het maken van moeilijke beslissingen, ons ondersteunen in tijden van nood en zelfs ons creatief inspireren. De impact op ons persoonlijke en professionele leven zou immens zijn. Denk aan een wereld waarin je nooit meer vastzit in verkeersopstoppingen dankzij real-time, voorspellend verkeersmanagement, of een wereld waarin gezondheidszorg op maat wordt geleverd door een AI die je unieke medische geschiedenis begrijpt en constant monitort.\n" +
                "\n" +
                "Maar \"Elysium Innovatus\" gaat verder dan technologie alleen. Het project richt zich ook op duurzaamheid en het bevorderen van een groenere planeet. Innovatieve technieken voor energieopslag en hernieuwbare energiebronnen zijn geïntegreerd in elke stap van het proces. Voor het eerst kunnen we een toekomst zien waarin schone energie de norm is, niet de uitzondering. Steden worden slimmer en groener, met gebouwen die zichzelf onderhouden en gemeenschappen die samenwerken om een duurzame leefomgeving te creëren.\n" +
                "\n" +
                "Onderwijs is een ander belangrijk aspect van \"Elysium Innovatus.\" Het project streeft ernaar om de manier waarop we leren en onderwijzen te hervormen. Door gebruik te maken van gepersonaliseerde leerprogramma's en virtuele realiteit, kunnen studenten over de hele wereld toegang krijgen tot hoogwaardig onderwijs, ongeacht hun locatie of sociaaleconomische status. De kloof tussen arm en rijk kan eindelijk worden gedicht door middel van educatieve gelijkheid en toegang tot kennis.\n" +
                "\n" +
                "De infrastructuur van \"Elysium Innovatus\" is even indrukwekkend als de visie zelf. Een wereldwijd netwerk van onderzoekscentra en innovatiehubs zal wetenschappers, ingenieurs en creatievelingen van alle disciplines samenbrengen om te collaboreren en te innoveren. Deze centra fungeren als broedplaatsen voor nieuwe ideeën en technologieën, waar de grenzen van het mogelijke voortdurend worden verlegd. Van biotechnologie tot kwantumcomputing, elke denkbare discipline wordt aangesproken en geïntegreerd in een holistische benadering van vooruitgang.\n" +
                "\n" +
                "\"Elysium Innovatus\" is niet alleen een project; het is een beweging. Het nodigt iedereen uit om deel te nemen, bij te dragen en te profiteren van de ongelooflijke vooruitgangen die het mogelijk maakt. Burgers, overheden, bedrijven en non-profitorganisaties werken samen in een ongekend partnerschap om de dromen van morgen vandaag te realiseren. Het is een oproep tot actie, een kans om deel uit te maken van iets veel groters dan wijzelf.\n" +
                "\n" +
                "In de kern draait \"Elysium Innovatus\" om hoop en de onbegrensde mogelijkheden van de menselijke geest. Het herinnert ons eraan dat, hoewel we geconfronteerd worden met talloze uitdagingen, onze capaciteit om te creëren, te ontdekken en te groeien, eindeloos is. Dit project is een bewijs van wat we kunnen bereiken wanneer we ons verenigen in de zoektocht naar een betere toekomst. \"Elysium Innovatus\" is meer dan een droom; het is onze nieuwe realiteit in wording.",
                "## Opgave: Woordenboek Omkeren ##\n" +
                        "**Probleem:**\n" +
                        "Je krijgt een woordenboek (dictionary) waarin de waarden lijsten van integers zijn. Schrijf een Python-functie invert_dictionary die het woordenboek omkeert. In het omgekeerde woordenboek worden de integers de sleutels en de oorspronkelijke sleutels worden opgenomen in een lijst als hun waarden.\n" +
                        "\n" +
                        "**Specificaties:**\n" +
                        "1. De functie moet `invert_dictionary(d: Dict[str, List[int]]) -> Dict[int, List[str]]` heten.\n" +
                        "2. Alle integers in de originele lijsten zijn uniek over alle sleutels heen.\n" +
                        "3. De volgorde van de lijsten in de output woordenboek hoeft niet hetzelfde te zijn als in de input.",
                "## Challenge: Palindrome Checker ##\n" +
                        "Write a function called `is_palindrome` that takes a single string as an argument and returns `True` if the string is a palindrome and `False` otherwise. A palindrome is a word, phrase, number, or other sequence of characters that reads the same forward and backward (ignoring spaces, punctuation, and capitalization).\n" +
                        "\n" +
                        "**Example:**\n" +
                        "- `is_palindrome(\"A man, a plan, a canal, Panama!\")` should return `True`\n" +
                        "- `is_palindrome(\"racecar\")` should return `True`\n" +
                        "- `is_palindrome(\"hello\")` should return `False`\n\n" +
                        "**Requirements:**\n" +
                        "- Ignore capitalization (case-insensitive).\n" +
                        "- Ignore spaces, punctuation, and other non-alphanumeric characters.\n\n" +
                        "**Hint:**\n" +
                        "You can use the re module to help with ignoring non-alphanumeric characters."

        };
        ProjectJson  pj = new ProjectJson();
        pj.setName(projectTitles[index]);
        pj.setDescription(projectDescriptions[index]);
        pj.setDeadline(
                OffsetDateTime.now().plusMonths(3)
        );
        pj.setVisible(true);
        pj.setMaxScore(20);

        ResponseEntity resp = projectController.createProject(course.courseId(), pj, auth);
        if (resp.getStatusCode().is2xxSuccessful()) {
            Logger.getGlobal().info("Success project create");
            return (ProjectResponseJsonWithStatus) resp.getBody();
        } else {
            Logger.getGlobal().info("Error while creating project: " + resp.getStatusCode() + " " + resp.getBody());
        }
        return null;
    }

    private static UserEntity getStudentUserEntity(int i, int j) {
        int idx = i *10 + j;
        return new UserEntity(
                "student",
                "number ".concat(String.valueOf(idx)),
                "student.number".concat(String.valueOf(idx)).concat("@ugent.be"),
                UserRole.student,
                "azure_id_number_".concat(String.valueOf(idx)),
                String.valueOf(idx * 1000)
        );
    }

}
