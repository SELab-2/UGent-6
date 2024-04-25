-- The table creation statements you provided go here, unchanged.
-- Skipping this part for brevity as you've already provided it above.

-- Inserting into `users`
INSERT INTO users (name, surname, email, azure_id, role) VALUES
                                                             ('John', 'Doe', 'john.doe@example.com', 'token_1', 'student'),
                                                             ('Jane', 'Smith', 'jane.smith@example.com', 'token_2', 'teacher'),
                                                             ('Bob', 'Brown', 'bob.brown@example.com', 'token_3', 'admin'),
                                                             ('Alice', 'Johnson', 'alice.johnson@example.com', 'token_4', 'student'),
                                                             ('Charlie', 'Davis', 'charlie.davis@example.com', 'token_5', 'teacher');

-- Inserting into `courses`
INSERT INTO courses (course_id,course_name, description, course_year) VALUES
                                                             (1,'Math 101', 'Introduction to Mathematics',2023),
                                                             (2,'Science 101', 'Basics of Scientific Method',2023),
                                                             (3,'History 101', 'World History Overview',2023),
                                                             (4,'Computer Science 101', 'Introduction to Computing',2023),
                                                             (5,'English 101', 'English Literature',2023);

-- Inserting into `course_users`
-- Assume course_id and user_id start from 1 and match accordingly
INSERT INTO course_users (course_id, user_id, course_relation) VALUES
                                                                   (1, 1, 'enrolled'),
                                                                   (2, 1, 'enrolled'),
                                                                   (3, 2, 'creator'),
                                                                   (4, 3, 'course_admin'),
                                                                   (5, 4, 'enrolled');

-- Inserting into `files`
-- Assume files are uploaded by different users
INSERT INTO files (file_path, file_name, uploaded_by) VALUES
                                                          ('/path/to/file1', 'file1.txt', 1),
                                                          ('/path/to/file2', 'file2.txt', 2),
                                                          ('/path/to/file3', 'file3.txt', 3),
                                                          ('/path/to/file4', 'file4.txt', 4),
                                                          ('/path/to/file5', 'file5.txt', 1),
                                                          ('/path/to/file6', 'file6.txt', 2),
                                                          ('/path/to/file7', 'file7.txt', 3),
                                                          ('/path/to/file8', 'file8.txt', 4),
                                                          ('/path/to/file9', 'file9.txt', 1),
                                                          ('/path/to/file10', 'file10.txt', 2),
                                                          ('/path/to/file11', 'file11.txt', 3),
                                                          ('/path/to/file12', 'file12.txt', 4),
                                                          ('/path/to/file13', 'file13.txt', 5),
                                                          ('/path/to/file14', 'file14.txt', 4),
                                                          ('/path/to/file15', 'file15.txt', 5),
                                                          ('/path/to/file16', 'file16.txt', 1),
                                                          ('/path/to/file17', 'file17.txt', 2),
                                                          ('/path/to/file18', 'file18.txt', 3),
                                                          ('/path/to/file19', 'file19.txt', 4),
                                                          ('/path/to/file20', 'file20.txt', 1),
                                                          ('/path/to/file21', 'file21.txt', 2),
                                                          ('/path/to/file22', 'file22.txt', 3);


-- Assume tests are created before projects for foreign key constraints
-- Inserting into `tests`
INSERT INTO tests (docker_image, docker_test, structure_test_id) VALUES
                                                                     ('docker/image1', 16, 17),
                                                                     ('docker/image2', 5, 6),
                                                                     ('docker/image3', 8, 9),
                                                                     ('docker/image4', 12, 13),
                                                                     ('docker/image5', 14, 15);

-- Inserting into `group_clusters`
INSERT INTO group_clusters (course_id, cluster_name, max_size, group_amount) VALUES
                                                                                 (1, 'Project: priemgetallen', 4, 20),
                                                                                 (2, 'Analyse van alkanen', 3, 10),
                                                                                 (3, 'Groepswerk industriële revolutie', 5, 13),
                                                                                 (4, 'Linux practica', 2, 100),
                                                                                 (5, 'Review: A shaskespeare story', 3, 30);

-- Inserting into `groups`
INSERT INTO groups (group_name, group_cluster) VALUES
                                                   ('Group 1', 1),
                                                   ('Group 2', 2),
                                                   ('Group 3', 3),
                                                   ('Group 4', 4),
                                                   ('Group 5', 5);

-- Inserting into `group_users`
-- Linking users to groups, assuming group_id and user_id start from 1
INSERT INTO group_users (group_id, user_id) VALUES
                                                (1, 1),
                                                (2, 2),
                                                (3, 3),
                                                (4, 4),
                                                (5, 5);



-- Inserting into `solutions`
-- Linking solutions to projects and groups
INSERT INTO projects (course_id, test_id, project_name, description, group_cluster_id, max_score, deadline)
VALUES
    (1, 1, 'Math project 1', 'Solve equations', 1, 20, '2024-03-20 09:00:00+02'),
    (2, 2, 'Science Lab 1', 'Aparte reeks met enkel de opdracht-oefening zodat de interface van Dodona de deadline duidelijk maakt. Deze opdracht komt uit de hoofdstuk 5 en de eerdere oefeningen uit die reeks zullen je helpen tot een juiste oplossing te komen.

Jullie oplossing wordt geëvalueerd op basis van uitvoeringstijd (**50%**), geheugengebruik (25%) en codestijl (25%).

Jullie moeten de opdracht "Global alignment" oplossen door zelf het geziene algoritme te implementeren. Jullie mogen de BioPython functionaliteit in [Bio.Align](https://biopython.org/docs/latest/api/Bio.Align.html?highlight=align#Bio.Align.PairwiseAligner) gebruiken voor de [BLOSUM62 matrix](https://github.com/biopython/biopython/blob/fb0e7f522d714d76eec42f5fd428eb7942a11dde/Bio/Align/substitution_matrices/data/BLOSUM62#L4) en te debuggen. Zo is de volgende code toegestaan:
```python
from Bio.Align import substitution_matrices
blosum62 = substitution_matrices.load(“BLOSUM62”)
```

Voor je uiteindelijke implementatie van global alignment moet je echter tonen dat je zelf het algoritme kunt implementeren. Je mag geen bestaande implementatie van dit algoritme indienen als oplossing.

Bovendien moeten jullie een extra functie `global_multiple_alignment` implementeren. Die leest meerdere sequencties in, berekent de global alignment score voor elk mogelijk paar en schrijft deze uit naar een distance matrix. De uitvoer moet opmaakopties ondersteunen, zoals die in [numpy.savetxt](https://numpy.org/doc/stable/reference/generated/numpy.savetxt.html#numpy-savetxt). Enkel de argumenten `fmt`, `delimiter` en `header` zijn verplichte functionaliteit.

Gezien dit gemakkelijk te paralleliseren is, kan het interessant zijn om de 4 vCPUs op het [benchmarkplatform](https://github.ugent.be/computationele-biologie/benchmarks-2024) ten volste te gebruiken om je uitvoeringstijd te verlagen. Er wordt een tijdslimiet van 40 seconden gebruikt. Als je oplossing deze niet haalt, dan zal er niet voor elke dataset een benchmarkresultaat verschijnen.

Er kunnen nog veel andere redenen zijn waarom de algemene benchmark voor je oplossing geen correct resultaat weergeeft. Breng ons hiervan op de hoogte, maar test en benchmark vooral zelf je code en vat je bevindingen samen in je codedocumentatie, zodat hiermee bij het verbeteren rekening mee kan worden gehouden.

Andere en meer geavanceerde manieren om Multiple Sequence Alignment te doen komen aan bod in latere hoofdstukken (7 en 10) van het handboek, de oefening ["Global multiple alignment"](https://dodona.be/nl/activities/2095074352/) en zijn ook [online](https://www.ebi.ac.uk/jdispatcher/msa) te vinden. Deze zijn allemaal niet nodig voor deze opdracht.

Probeer gelijkaardig aan volgend functievoorschrift en doctest te werken, zodat de benchmark je code kan testen.
```python
def global_multiple_alignment(infile: str | Path, output: str | Path | None = None, **kwargs) -> list[list[int]] | None:
    pass
```
```python
>>> len(global_multiple_alignment(''data/global_alignment/data_10.fna''))
10
>>> from pathlib import Path
>>> matrix = Path(''matrix.txt'')
>>> global_multiple_alignment(Path(''data/global_alignment/data_10.fna''), matrix)
>>> len(open(matrix).readlines())
10
>>> print(matrix.read_text())
0 429 252 285 358 124 155 328 226 212
429 0 185 201 257 60 85 257 183 156
252 185 0 65 148 -14 25 137 72 65
285 201 65 0 213 -135 -41 80 54 -16
358 257 148 213 0 -70 34 149 68 52
124 60 -14 -135 -70 0 10 73 -15 23
155 85 25 -41 34 10 0 60 80 67
328 257 137 80 149 73 60 0 142 155
226 183 72 54 68 -15 80 142 0 96
212 156 65 -16 52 23 67 155 96 0
<BLANKLINE>
```', 2, 6, '2024-06-22 12:00:00+02'),
    (3, 3, 'History Essay 1', 'Discuss historical event', 3, NULL, '2024-03-22 12:00:00+02'),
    (4, 4, 'Programming Assignment 1', 'Write code', 4, 4, '2024-03-23 14:45:00+02'),
    (5, 5, 'Literature Analysis', 'Analyze text', 5, 10, '2024-03-24 10:00:00+02');


-- Inserting into `group_grades`
-- Assign grades to group solutions
INSERT INTO group_feedback(group_id, project_id, grade, feedback) VALUES
                                                                      (1, 1, 95.0, ''),
                                                                      (2, 2, 88.5, ''),
                                                                      (3, 3, NULL, ''),
                                                                      (4, 4, 89.0, ''),
                                                                      (5, 5, 94.5, '');


INSERT INTO submissions (
    project_id,
    group_id,
    file_id,
    structure_accepted,
    docker_accepted,
    structure_feedback,
    docker_feedback
) VALUES
      (1, 1, 1, true, true, NULL, NULL),
      (2, 2, 2, false, true, 'ERROR: .....', NULL),
      (3, 3, 3, true, false, NULL, 'Docker configuration needs improvement'),
      (4, 4, 4, false, false, 'Structure needs improvement', 'Docker configuration needs improvement');



-- Makes user with id 1 the creator of courses
UPDATE course_users SET course_relation = 'creator' WHERE user_id = 1
