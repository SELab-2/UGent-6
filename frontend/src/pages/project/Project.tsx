import { Button, Card, Col, Row, Space, Spin, theme } from "antd"
import { useEffect, useState } from "react"
import { ApiRoutes, GET_Responses } from "../../@types/requests"
import Markdown from "react-markdown"
import { Prism as SyntaxHighlighter } from "react-syntax-highlighter"
import { oneDark, oneLight } from "react-syntax-highlighter/dist/esm/styles/prism"
import useApp from "../../hooks/useApp"
import { PlusOutlined } from "@ant-design/icons"
import { useTranslation } from "react-i18next"
import { useNavigate, useParams } from "react-router-dom"
import { AppRoutes } from "../../@types/routes"
import GroupsCard from "../course/components/groupTab/GroupsCard"
import SubmissionCard from "./components/SubmissionCard"
import useCourse from "../../hooks/useCourse"

//  dracula, darcula,oneDark,vscDarkPlus  | prism, base16AteliersulphurpoolLight, oneLight

export type ProjectType = GET_Responses[ApiRoutes.PROJECT]

const Project = () => {
  const { token } = theme.useToken()
  const { t } = useTranslation()
  const app = useApp()
  const navigate = useNavigate()
  const course = useCourse()
  const { projectId } = useParams()
  const [project, setProject] = useState<ProjectType | null>(null)

  useEffect(() => {
    // TODO make api call to

    setTimeout(() => {
      setProject({
        course: {
          courseId: 1,
          name: "course",
          url: "course_url",
        },
        deadline: "2024-03-29T17:00:00",
        description:
          "Aparte reeks met enkel de opdracht-oefening zodat de interface van Dodona de deadline duidelijk maakt. Deze opdracht komt uit de hoofdstuk 5 en de eerdere oefeningen uit die reeks zullen je helpen tot een juiste oplossing te komen.\r\n\r\nJullie oplossing wordt geëvalueerd op basis van uitvoeringstijd (**50%**), geheugengebruik (25%) en codestijl (25%).\r\n\r\nJullie moeten de opdracht \"Global alignment\" oplossen door zelf het geziene algoritme te implementeren. Jullie mogen de BioPython functionaliteit in [Bio.Align](https://biopython.org/docs/latest/api/Bio.Align.html?highlight=align#Bio.Align.PairwiseAligner) gebruiken voor de [BLOSUM62 matrix](https://github.com/biopython/biopython/blob/fb0e7f522d714d76eec42f5fd428eb7942a11dde/Bio/Align/substitution_matrices/data/BLOSUM62#L4) en te debuggen. Zo is de volgende code toegestaan:\r\n```python\r\nfrom Bio.Align import substitution_matrices\r\nblosum62 = substitution_matrices.load(“BLOSUM62”)\r\n```\r\n\r\nVoor je uiteindelijke implementatie van global alignment moet je echter tonen dat je zelf het algoritme kunt implementeren. Je mag geen bestaande implementatie van dit algoritme indienen als oplossing.\r\n\r\nBovendien moeten jullie een extra functie `global_multiple_alignment` implementeren. Die leest meerdere sequencties in, berekent de global alignment score voor elk mogelijk paar en schrijft deze uit naar een distance matrix. De uitvoer moet opmaakopties ondersteunen, zoals die in [numpy.savetxt](https://numpy.org/doc/stable/reference/generated/numpy.savetxt.html#numpy-savetxt). Enkel de argumenten `fmt`, `delimiter` en `header` zijn verplichte functionaliteit.\r\n\r\nGezien dit gemakkelijk te paralleliseren is, kan het interessant zijn om de 4 vCPUs op het [benchmarkplatform](https://github.ugent.be/computationele-biologie/benchmarks-2024) ten volste te gebruiken om je uitvoeringstijd te verlagen. Er wordt een tijdslimiet van 40 seconden gebruikt. Als je oplossing deze niet haalt, dan zal er niet voor elke dataset een benchmarkresultaat verschijnen.\r\n\r\nEr kunnen nog veel andere redenen zijn waarom de algemene benchmark voor je oplossing geen correct resultaat weergeeft. Breng ons hiervan op de hoogte, maar test en benchmark vooral zelf je code en vat je bevindingen samen in je codedocumentatie, zodat hiermee bij het verbeteren rekening mee kan worden gehouden.\r\n\r\nAndere en meer geavanceerde manieren om Multiple Sequence Alignment te doen komen aan bod in latere hoofdstukken (7 en 10) van het handboek, de oefening [\"Global multiple alignment\"](https://dodona.be/nl/activities/2095074352/) en zijn ook [online](https://www.ebi.ac.uk/jdispatcher/msa) te vinden. Deze zijn allemaal niet nodig voor deze opdracht.\r\n\r\nProbeer gelijkaardig aan volgend functievoorschrift en doctest te werken, zodat de benchmark je code kan testen.\r\n```python\r\ndef global_multiple_alignment(infile: str | Path, output: str | Path | None = None, **kwargs) -> list[list[int]] | None:\r\n    pass\r\n```\r\n```python\r\n>>> len(global_multiple_alignment('data/global_alignment/data_10.fna'))\r\n10\r\n>>> from pathlib import Path\r\n>>> matrix = Path('matrix.txt')\r\n>>> global_multiple_alignment(Path('data/global_alignment/data_10.fna'), matrix)\r\n>>> len(open(matrix).readlines())\r\n10\r\n>>> print(matrix.read_text())\r\n0 429 252 285 358 124 155 328 226 212\r\n429 0 185 201 257 60 85 257 183 156\r\n252 185 0 65 148 -14 25 137 72 65\r\n285 201 65 0 213 -135 -41 80 54 -16\r\n358 257 148 213 0 -70 34 149 68 52\r\n124 60 -14 -135 -70 0 10 73 -15 23\r\n155 85 25 -41 34 10 0 60 80 67\r\n328 257 137 80 149 73 60 0 142 155\r\n226 183 72 54 68 -15 80 142 0 96\r\n212 156 65 -16 52 23 67 155 96 0\r\n<BLANKLINE>\r\n```",
        name: "Global alignment",
        projectId: 1,
        submission_url: "submission_url",
        tests_url: "tests_url",
      })
    }, 300)
  }, [])

  const CodeBlock = {
    code({ children, className, node, ...rest }: any) {
      const match = /language-(\w+)/.exec(className || "")
      return match ? (
        <SyntaxHighlighter
          {...rest}
          PreTag="div"
          children={String(children).replace(/\n$/, "")}
          language={match[1]}
          style={app.theme === "light" ? oneLight : oneDark}
        />
      ) : (
        <code
          {...rest}
          className={className}
        >
          {children}
        </code>
      )
    },
  }


  // if (!project) {
  //   return (
  //     <div style={{ display: "flex", justifyContent: "center", alignItems: "center", height: "100vh" }}>
  //       <Spin size="large" />
  //     </div>
  //   )
  // }

  return (
    <div style={{ margin: "3rem 0", width: "100%" }}>
      <Row
        justify="center"
        gutter={[32, 32]}
        style={{ width: "100%" }}
      >
        <Col
          lg={16}
          md={16}
          sm={24}
          xs={24}
        >
          <Card
            styles={{
              header: {
                background: token.colorPrimaryBg,
              },
              title: {
                fontSize: "1.1em",
              },
              body: {
                textWrap: "wrap",
              },
            }}
            style={{ width: "100%", marginBottom: "3rem" }}
            title={project?.name}
            loading={!project}
          >
            {project && <Markdown components={CodeBlock}>{project.description}</Markdown>}
          </Card>
        </Col>
        <Col
          lg={8}
          md={8}
          sm={24}
          xs={24}
        >
          <SubmissionCard projectId={Number(projectId)} courseId={course.courseId}/>
         

          <GroupsCard
            cardProps={{
              title: t("course.groups"),
              style: { width: "100%" },
            }}
            courseId={project?.course.courseId ?? null}
          />
        </Col>
      </Row>
    </div>
  )
}
export default Project
