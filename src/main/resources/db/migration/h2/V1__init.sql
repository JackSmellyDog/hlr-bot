CREATE TABLE bot_user (
	id BIGINT NOT NULL,
	api_key VARCHAR(255),
	first_name VARCHAR(255),
	last_name VARCHAR(255),
	language_code VARCHAR(255),
	state VARCHAR(255),
	user_name VARCHAR(255),
	CONSTRAINT CONSTRAINT_BOT_USER PRIMARY KEY (id)
);
CREATE UNIQUE INDEX PRIMARY_KEY_BOT_USER ON bot_user(id);


CREATE TABLE files (
	id VARCHAR(255) NOT NULL,
	deleted BOOLEAN NOT NULL,
	extension VARCHAR(255) NOT NULL,
	full_path VARCHAR(255) NOT NULL,
	mime_type VARCHAR(255),
	real_file_name VARCHAR(255) NOT NULL,
	received_file_name VARCHAR(255) NOT NULL,
	CONSTRAINT CONSTRAINT_FILES PRIMARY KEY (id)
);
CREATE UNIQUE INDEX PRIMARY_KEY_FILES ON files(id);