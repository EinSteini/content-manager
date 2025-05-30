# Content Manager
This project is dedicated to simplify the distribution of content to multiple Platforms.

## Support

| **Platform** | *Text* | *Image* | *Image Gallery* |
|--------------|--------|---------|-----------------|
| Bluesky      | ✅      | ✅       | 🧭              |
| Twitter (X)  | ✅      | 🧭      | 🧭              |
| Threads      | ✅      | ❌       | ❌               |

✅: Supported and working <br>
❌: Not supported <br>
🧭: Planned / Work in progress

## Usage
The easiest way to use this project is by using Docker Compose.
Create an `.env` file in the root directory and fill it with the required variables (a template can be found in `example.env`).
Then run the following command in the project's root directory:
```
docker compose run --build --rm --service-ports app
```
This builds the Docker image and starts the application in interactive mode.

## Known Issues
- If you want to use multiple platforms that require OAuth authentication (currently Twitter and Threads), you need to authenticate them in different steps. 
  For example, if you want to use both, you need to authenticate Twitter first, then Threads. (Issue #33)
