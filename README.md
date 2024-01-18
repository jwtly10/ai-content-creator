# AI Content Generator

This app is a Java Spring Boot application designed to leverage viral TikTok trends by generating content from Reddit
posts. It provides a RESTFul interface for content generation, and a scheduled job, to ensure a seamless user
experience.

This app is paired with a React\Typescript frontend to provide a user interface for generating content. The frontend
site repo can be found [here](https://github.com/jwtly10/reddreelstoryz-frontend).

## Demo:

### Website:
https://github.com/jwtly10/ai-content-creator/assets/39057715/b6d508b1-bd86-42ff-99f0-ee6b8d38dc7a
### Example Output:
https://github.com/jwtly10/ai-content-creator/assets/39057715/6562e43a-7b4a-4781-974e-86d57e3cb520


## Features

- **Spring Security with JWT Authentication:** Ensuring secure user access to content generation features.
- **Rate Limiting of APIs:** Preventing abuse and ensuring fair use of API resources.
- **Scheduled Async Jobs:** Provides a good user experience, by allowing users to submit jobs and return to the site
  later to see generated content.
- **AWS MySQL RDS** For storing user data and content generation jobs.
- **Docker:** Spins up required dependencies for the application to run.
- **GitHub Actions** : Automates the build and testing process, making new features available quickly.
- **Extensive Unit & Integration Testing** : Ensures that the application is robust and reliable, and certain key
  methods are tested for correctness.

## How it works

The application parses Reddit posts using a proxy, which is then queued as a new job in the database. A scheduled job
then processes the job queue, and generates content for each job. Endpoints allow checking the status of a given
processID, and retrieving the generated content, or just retrieving all non-deleted videos, along with their state and
content if generated.

A few external APIs are used to help generate the content:

- **Google TTS service:** Allows the application to generate audio from text.
- **Gentle Aligner Docker Image:** Aids with helping align the audio with the text for SRT (subtitle) generation.
- **OpenAI API:** Used to determine a gender from the given content, before generating the audio.

Java image & graphics libraries are also used to generate the title image card for the video.

FFmpeg is the underlying technology used to generate the content. Within the job logic, FFmpeg is used to perform
various media modifications including stitch, crop and resizing the video, also appending subtitles. The video is then
uploaded to an S3 bucket, and the URL is stored in the database, ready for the user to retrieve.

