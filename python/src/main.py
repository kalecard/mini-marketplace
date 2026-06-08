from contextlib import asynccontextmanager

from fastapi import FastAPI, Request
from strawberry.fastapi import GraphQLRouter

from src.db.migrate import run_migration
from src.db.pool import create_pool
from src.graphql.schema import schema
from src.services.campaign_service import CampaignService
from src.services.submission_service import SubmissionService


@asynccontextmanager
async def lifespan(app: FastAPI):
    pool = await create_pool()
    await run_migration(pool)
    app.state.pool = pool
    app.state.campaign_service = CampaignService(pool)
    app.state.submission_service = SubmissionService(pool)
    yield
    await pool.close()


async def get_context(request: Request) -> dict:
    return {
        "campaign_service": request.app.state.campaign_service,
        "submission_service": request.app.state.submission_service,
    }


graphql_app = GraphQLRouter(schema, context_getter=get_context)

app = FastAPI(lifespan=lifespan)
app.include_router(graphql_app, prefix="/graphql")


if __name__ == "__main__":
    import uvicorn

    uvicorn.run("src.main:app", host="0.0.0.0", port=8080)
