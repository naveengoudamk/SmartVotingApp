import React from 'react';

const Gallery = () => {
    const images = [
        { src: '/snap/AppDashboard.png', alt: 'App Dashboard', title: 'Admin Dashboard' },
        { src: '/snap/AppLogin.png', alt: 'App Login', title: 'Secure Login' },
        { src: '/snap/Flowchart.png', alt: 'Flowchart', title: 'Process Flow' },
        { src: '/snap/SystemArchitecture.jpeg', alt: 'System Architecture', title: 'System Architecture' },
        { src: '/snap/use_case_diagram.jpeg', alt: 'Use Case Diagram', title: 'Use Case Diagram' },
        { src: '/snap/uml_diagram.jpeg', alt: 'UML Diagram', title: 'UML Diagram' },
    ];

    return (
        <section className="py-16 bg-gradient-to-br from-slate-50 to-slate-100" id="gallery">
            <div className="container mx-auto px-6">
                <h2 className="text-4xl font-bold text-center mb-4 text-emerald-800">
                    App Visuals & Architecture
                </h2>
                <p className="text-center text-slate-600 mb-12 max-w-2xl mx-auto">
                    Explore the interface and structural design of the Smart Voting App.
                </p>

                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
                    {images.map((img, index) => (
                        <div
                            key={index}
                            className="group relative bg-white rounded-xl shadow-lg overflow-hidden hover:shadow-2xl transition-all duration-300 transform hover:-translate-y-1"
                        >
                            <div className="aspect-w-16 aspect-h-9 overflow-hidden bg-gray-100 flex items-center justify-center h-64">
                                <img
                                    src={img.src}
                                    alt={img.alt}
                                    className="w-full h-full object-contain p-2 group-hover:scale-105 transition-transform duration-300"
                                />
                            </div>
                            <div className="p-4 border-t border-slate-100">
                                <h3 className="text-xl font-semibold text-slate-800 text-center">
                                    {img.title}
                                </h3>
                            </div>
                        </div>
                    ))}
                </div>
            </div>
        </section>
    );
};

export default Gallery;
