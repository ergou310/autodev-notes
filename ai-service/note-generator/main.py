from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

app = FastAPI(
    title="AutoDev Notes - AI Service",
    description="智能教育助教平台 AI 服务",
    version="1.0.0"
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


@app.get("/")
async def root():
    return {"message": "AutoDev Notes AI Service", "version": "1.0.0"}


@app.get("/health")
async def health_check():
    return {"status": "healthy"}
