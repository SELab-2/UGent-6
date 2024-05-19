import { ProjectSubmissionsType } from "./SubmissionsTab"
import { saveAs } from "file-saver"
import { unparse } from "papaparse"

function exportSubmissionStatusToCSV(submissions: ProjectSubmissionsType[]): void {
  const csvData = submissions.map((submission) => {
    const groupId = submission.group.groupId
    const groupName = submission.group.name
    let submissionTime = "Not submitted"
    let structureStatus = "Not submitted"
    let dockerStatus = "Not submitted"
    if (submission.submission) {
       submissionTime = submission.submission.submissionTime
       structureStatus = submission.submission?.structureAccepted ? "Accepted" : "Rejected"
       dockerStatus = submission.submission?.dockerStatus || "Unknown"
    }

    const students = submission.group.members.map((member) => `${member.name} (${member.studentNumber ?? "N/A"})`).join("; ")

    return {
      groupId,
      groupName,
      structureStatus,
      dockerStatus,
      submissionTime,
      students,
    }
  })

  const csvString = unparse(csvData)

  const blob = new Blob([csvString], { type: "text/csv;charset=utf-8;" })
  saveAs(blob, "project_submissions.csv")
}



function exportToUfora(submissions: ProjectSubmissionsType[],maxScore:number): void {

  const evaluationHeader = `Evaluation 1 Exercise 1 Points Grade <Numeriek MaxAantalPunten:${maxScore}>`;

  const csvData = submissions.flatMap(submission => 
    submission.group.members.map(member => ({
      OrgDefinedId: `#${member.studentNumber}`,
      LastName: member.name.split(' ').slice(-1)[0],
      FirstName: member.name.split(' ').slice(0, -1).join(' '),
      Email: member.email,
      [evaluationHeader]: submission.feedback?.score ?? "",
      "End-of-Line Indicator": "#"
    }))
  );
  console.log(submissions, csvData);

  const csvString = unparse(csvData, {
    quotes: true,
    header: true
  });
  
  const blob = new Blob([csvString], { type: 'text/csv;charset=utf-8;' });
  saveAs(blob, 'ufora_submissions.csv');
}

export { exportSubmissionStatusToCSV,exportToUfora }
