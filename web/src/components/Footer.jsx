import React from 'react';

const Footer = () => {
    return (
        <footer className="py-8 border-t border-slate-800 text-center text-[var(--color-text-dim)]">
            <div className="container">
                <p>&copy; {new Date().getFullYear()} SmartVoting App. All rights reserved.</p>
            </div>
        </footer>
    );
};

export default Footer;
