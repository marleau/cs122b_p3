-- change delimiter to $$
DELIMITER $$

CREATE PROCEDURE add_movie (m_title VARCHAR(100), m_year INT(11), m_director VARCHAR(100), star_first VARCHAR(50), star_last VARCHAR(50), genre_name VARCHAR(32))
BEGIN
    DECLARE starID INT(11) DEFAULT NULL;
    DECLARE genreID INT(11) DEFAULT NULL;
    DECLARE movieID INT(11) DEFAULT NULL;

    SET starID = (SELECT id FROM stars WHERE first_name=star_first AND last_name=star_last);
    SET genreID = (SELECT id from genres WHERE name=genre_name);
    SET movieID = (SELECT id from movies WHERE title=m_title AND year=m_year AND director=m_director);

    -- if movie does not exist, create it
    IF movieID IS NULL THEN
        INSERT INTO movies (title, year, director) VALUES (m_title, m_year, m_director);
        SET movieID = (SELECT id from movies WHERE title=m_title AND year=m_year AND director=m_director);
    END IF;

    -- if star exists, link it to movie
    -- else, create star then link to movie
    IF starID IS NOT NULL THEN
        INSERT INTO stars_in_movies VALUES (starID, movieID);
    ELSE
        INSERT INTO stars (first_name, last_name) VALUES (star_first, star_last);
        SET starID = (SELECT id FROM stars WHERE first_name=star_first AND last_name=star_last);
        INSERT INTO stars_in_movies VALUES (starID, movieID);
    END IF;

    -- if genre exists, link it to movie
    -- else, create genre then link to movie
    IF genreID IS NOT NULL THEN
        INSERT INTO genres_in_movies VALUES (genreID, movieID);
    ELSE
        INSERT INTO genres (name) VALUES (genre_name);
        SET genreID = (SELECT id from genres WHERE name=genre_name);
        INSERT INTO genres_in_movies VALUES (genreID, movieID);
    END IF;
END
$$

-- change delimiter to ;
DELIMITER ;

