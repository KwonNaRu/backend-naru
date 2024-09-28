DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_catalog.pg_tables WHERE tablename = 'users') THEN
        CREATE TABLE users (
            id SERIAL PRIMARY KEY,
            username VARCHAR(50) UNIQUE NOT NULL,
            email VARCHAR(100) UNIQUE NOT NULL,  -- 이메일 필드 추가
            password VARCHAR(255) NOT NULL
        );
    END IF;
END $$;