
package ohtu.verkkokauppa;

import org.junit.Before;
import org.junit.Test;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class KauppaTest {
    
    private Pankki pankki;
    private Viitegeneraattori viite;
    private Varasto varasto;
    private Kauppa kauppa;
    
    @Before
    public void setUp() {
        pankki = mock(Pankki.class);
        varasto = mock(Varasto.class);
        viite = mock(Viitegeneraattori.class);  
        kauppa = new Kauppa(varasto, pankki, viite); 
        
        // määritellään että viitegeneraattori palauttaa viitteen 42
        when(viite.uusi()).thenReturn(42);

        // määritellään että tuote numero 1 on maito jonka hinta on 5 ja saldo 10
        when(varasto.saldo(1)).thenReturn(10); 
        when(varasto.haeTuote(1)).thenReturn(new Tuote(1, "maito", 5));     
    }

    @Test
    public void ostoksenPaatyttyaPankinMetodiaTilisiirtoKutsutaan() {
        // tehdään ostokset
        kauppa.aloitaAsiointi();
        kauppa.lisaaKoriin(1);     // ostetaan tuotetta numero 1 eli maitoa
        kauppa.tilimaksu("pekka", "12345");

        // sitten suoritetaan varmistus, että pankin metodia tilisiirto on kutsuttu
        verify(pankki).tilisiirto(anyString(), anyInt(), anyString(), anyString(), anyInt());   
        // toistaiseksi ei välitetty kutsussa käytetyistä parametreista
    }
    
    /*  aloitetaan asiointi, koriin lisätään tuote, jota varastossa on ja suoritetaan
        ostos, eli kutsutaan metodia kaupan tilimaksu(), varmistettava että kutsutaan
        pankin metodia tilisiirto oikealla asiakkaalla, tilinumerolla ja summalla */
    @Test
    public void ostoksenPaatyttyaPankinMetodiaTilisiirtoKutsutaanOikein() {
        //Ostokset
        kauppa.aloitaAsiointi();
        kauppa.lisaaKoriin(1);
        kauppa.tilimaksu("pekka", "12345");
        
        //Varmistus
        verify(pankki).tilisiirto(eq("pekka"), anyInt(), eq("12345"), anyString(), eq(5));  
    }
    
    /*  aloitetaan asiointi, koriin lisätään kaksi eri tuotetta, joita varastossa
        on ja suoritetaan ostos, varmistettava että kutsutaan pankin metodia
        tilisiirto oikealla asiakkaalla, tilinumerolla ja summalla */
    @Test
    public void ostoksenPaatyttyaKahdellaEriTuotteellaPankinMetodiaTilisiirtoKutsutaanOikein() {
        //Varasto
        when(varasto.saldo(2)).thenReturn(10); 
        when(varasto.haeTuote(2)).thenReturn(new Tuote(2, "leipa", 4));
        
        //Ostokset
        kauppa.aloitaAsiointi();
        kauppa.lisaaKoriin(1);
        kauppa.lisaaKoriin(2);
        kauppa.tilimaksu("pekka", "12345");
        
        //Varmistus
        verify(pankki).tilisiirto(eq("pekka"), anyInt(), eq("12345"), anyString(), eq(9));  
    }
    
    /*  aloitetaan asiointi, koriin lisätään kaksi samaa tuotetta jota on varastossa
        tarpeeksi ja suoritetaan ostos, varmistettava että kutsutaan pankin metodia
        tilisiirto oikealla asiakkaalla, tilinumerolla ja summalla */
    @Test
    public void ostoksenPaatyttyaKahdellaSamallaTuotteellaPankinMetodiaTilisiirtoKutsutaanOikein() {
        //Ostokset
        kauppa.aloitaAsiointi();
        kauppa.lisaaKoriin(1);
        kauppa.lisaaKoriin(1);
        kauppa.tilimaksu("pekka", "12345");
        
        //Varmistus
        verify(pankki).tilisiirto(eq("pekka"), anyInt(), eq("12345"), anyString(), eq(10));  
    }
    
    /*  aloitetaan asiointi, koriin lisätään tuote jota on varastossa tarpeeksi
        ja tuote joka on loppu ja suoritetaan ostos, varmistettava että kutsutaan
        pankin metodia tilisiirto oikealla asiakkaalla, tilinumerolla ja summalla */
    @Test
    public void ostoksenPaatyttyaRiittavallaJaLoppuunmyydyllaTuotteellaPankinMetodiaTilisiirtoKutsutaanOikein() {
        //Varasto
        when(varasto.saldo(2)).thenReturn(0);
        when(varasto.haeTuote(2)).thenReturn(new Tuote(2, "kalja", 6));
        
        //Ostokset
        kauppa.aloitaAsiointi();
        kauppa.lisaaKoriin(1);
        kauppa.lisaaKoriin(2);
        kauppa.tilimaksu("pekka", "12345");
        
        //Varmistus
        verify(pankki).tilisiirto(eq("pekka"), anyInt(), eq("12345"), anyString(), eq(5));  
    }
    
    /*  varmistettava, että metodin aloitaAsiointi kutsuminen nollaa edellisen
        ostoksen tiedot (eli edellisen ostoksen hinta ei näy uuden ostoksen
        hinnassa), katso tarvittaessa apua projektin MockitoDemo testeistä! */
    @Test
    public void kaupanMetodiAloitaAsiointiNollaaEdellisenOstoksenTiedot() {
        //Ostokset
        kauppa.aloitaAsiointi();
        kauppa.lisaaKoriin(1);
        kauppa.tilimaksu("pekka", "12345");
        
        verify(pankki).tilisiirto(anyString(), anyInt(), anyString(), anyString(), eq(5));
        
        //Uusi ostos
        kauppa.aloitaAsiointi();
        kauppa.tilimaksu("pekka", "12345");
        
        //Varmistus
        verify(pankki).tilisiirto(anyString(), anyInt(), anyString(), anyString(), eq(0));
    }
    
    /*  varmistettava, että kauppa pyytää uuden viitenumeron jokaiselle
        maksutapahtumalle, katso tarvittaessa apua projektin MockitoDemo testeistä! */
    @Test
    public void kaupanMetodiTilimaksuPyytaaUudenViitenumeron() {
        //Uudet viitteet
        when(viite.uusi()).
                thenReturn(1).
                thenReturn(2).
                thenReturn(3);
        
        //Ostokset
        kauppa.aloitaAsiointi();
        kauppa.tilimaksu("pekka", "12345");
        
        //Varmistus
        verify(pankki).tilisiirto(anyString(), eq(1), anyString(), anyString(), anyInt());
        
        //Uusi ostos
        kauppa.aloitaAsiointi();
        kauppa.tilimaksu("pekka", "12345");
        
        //Varmistus
        verify(pankki).tilisiirto(anyString(), eq(2), anyString(), anyString(), anyInt());
        
        //Uusi ostos
        kauppa.aloitaAsiointi();
        kauppa.tilimaksu("pekka", "12345");
        
        //Varmistus
        verify(pankki).tilisiirto(anyString(), eq(3), anyString(), anyString(), anyInt());
    }
    @Test
    public void varastonMetodiPalautaVarastoonKutsutaanOikeallaTuotteella() {
        //Varasto
        Tuote tuote = new Tuote(2, "kalja", 6);
        when(varasto.saldo(2)).thenReturn(0); 
        when(varasto.haeTuote(2)).thenReturn(tuote);
        
        //Ostokset
        kauppa.aloitaAsiointi();
        kauppa.lisaaKoriin(2);
        kauppa.poistaKorista(2);
        
        //Varmistus
        verify(varasto).palautaVarastoon(eq(tuote));
    }
}
