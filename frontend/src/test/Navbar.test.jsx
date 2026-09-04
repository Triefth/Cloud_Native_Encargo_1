import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import Navbar from '../components/Navbar';

describe('Navbar Component', () => {
  it('renders branding title and subtitle', () => {
    render(<Navbar activeTab="health" setActiveTab={() => {}} bffStatus="online" activeToken={null} />);
    
    expect(screen.getByText('Telemedicina Rural')).toBeInTheDocument();
    expect(screen.getByText(/DSY1107/i)).toBeInTheDocument();
  });

  it('renders BFF Gateway ONLINE status badge when bffStatus is online', () => {
    render(<Navbar activeTab="health" setActiveTab={() => {}} bffStatus="online" activeToken={null} />);
    
    expect(screen.getByText(/BFF Gateway \(8080\): ONLINE/i)).toBeInTheDocument();
    expect(screen.getByText('Sin Token JWT')).toBeInTheDocument();
  });

  it('renders active token badge when activeToken is present', () => {
    render(<Navbar activeTab="health" setActiveTab={() => {}} bffStatus="online" activeToken="mock.token.jwt" />);
    
    expect(screen.getByText('Token JWT Activo')).toBeInTheDocument();
  });

  it('triggers setActiveTab callback when navigation tab button is clicked', () => {
    const setActiveTabMock = vi.fn();
    render(<Navbar activeTab="health" setActiveTab={setActiveTabMock} bffStatus="online" activeToken={null} />);
    
    const citasButton = screen.getByText('Agenda Citas');
    fireEvent.click(citasButton);
    expect(setActiveTabMock).toHaveBeenCalledWith('citas');
  });
});
