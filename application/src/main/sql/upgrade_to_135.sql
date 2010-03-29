DROP TABLE SURVEY_RESPONSE;

CREATE TABLE SURVEY_RESPONSE (
  RESPONSE_ID INTEGER AUTO_INCREMENT NOT NULL,
  INSTANCE_ID INTEGER NOT NULL,
  SURVEY_QUESTION_ID INTEGER NOT NULL,

  FREETEXT_VALUE TEXT,
  CHOICE_VALUE INTEGER,
  DATE_VALUE DATE,
  NUMBER_VALUE DECIMAL(16,5),
  MULTI_SELECT_VALUE TEXT,

  UNIQUE(INSTANCE_ID, SURVEY_QUESTION_ID),
  PRIMARY KEY(RESPONSE_ID),
  FOREIGN KEY(SURVEY_QUESTION_ID)
    REFERENCES SURVEY_QUESTIONS(SURVEYQUESTION_ID)
      ON DELETE NO ACTION
      ON UPDATE NO ACTION,
  FOREIGN KEY(INSTANCE_ID)
    REFERENCES SURVEY_INSTANCE(INSTANCE_ID)
      ON DELETE NO ACTION
      ON UPDATE NO ACTION,
  FOREIGN KEY(CHOICE_VALUE)
    REFERENCES QUESTION_CHOICES(CHOICE_ID)
      ON DELETE NO ACTION
      ON UPDATE NO ACTION
);

update DATABASE_VERSION set DATABASE_VERSION = 135 where DATABASE_VERSION = 134;