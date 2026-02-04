-- Insert initial Profile record
INSERT INTO profile (full_name, tagline, intro, degree, college, cgpa, about_summary, updated_at) VALUES
('Shobith R Acharya', 
 'Full Stack Developer | Flutter & Backend Specialist', 
 'Crafting scalable solutions and seamless mobile experiences',
 'Bachelor''s degree in Computer Science & Engineering',
 'MITE',
 7.5,
 'Hi, I''m Shobith R Acharya - a passionate and solution-driven Software Developer with a Bachelor''s degree in Computer Science & Engineering from MITE (2020-2024), graduating with a CGPA of 7.5.

I specialize in Flutter development, backend APIs, and SQL. I love creating seamless mobile experiences, optimizing performance, and building scalable solutions. Tech is not just my profession - it''s my playground.

I''m a collaborative team player who thrives in innovative environments, and I''m always up for a challenge that lets me push the boundaries of what''s possible.',
 NOW()
);

-- Insert initial ContactInfo record
INSERT INTO contact_info (email, phone, linkedin_url, github_url, portfolio_url) VALUES
('shobithracharya1816@gmail.com',
 '+91-9844371640',
 'https://linkedin.com/in/shobith-r-acharya-89a146222',
 'https://github.com/Shobith16',
 'https://shobith16.github.io/Portfolio/'
);
